package com.abhinav.ticketbooking.service.Booking;

import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingSeat;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.entity.show.ShowSeatStatus;

import com.abhinav.ticketbooking.repository.BookingRepository;
import com.abhinav.ticketbooking.repository.BookingSeatRepository;
import com.abhinav.ticketbooking.repository.ShowSeatRepository;
import com.abhinav.ticketbooking.entity.payment.Payment;
import com.abhinav.ticketbooking.entity.payment.PaymentStatus;
import com.abhinav.ticketbooking.repository.PaymentRepository;
import com.abhinav.ticketbooking.service.refund.RefundService;

import com.razorpay.RazorpayException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {
    private final PaymentRepository paymentRepository;
    private final RefundService refundService;

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;

    public BookingService(
            PaymentRepository paymentRepository, RefundService refundService, BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            ShowSeatRepository showSeatRepository) {
        this.paymentRepository = paymentRepository;
        this.refundService = refundService;

        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showSeatRepository = showSeatRepository;
    }

    // --------------------------------------------------
    // 1. Hold seats for 5 minutes
    // --------------------------------------------------

    @Transactional
    public Booking holdSeats(
            Long showId,
            List<Long> showSeatIds,
            String customerEmail) {

        if (showSeatIds == null || showSeatIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select at least one seat"
            );
        }

        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email is required"
            );
        }

        // Prevent duplicate seat IDs in the request
        Set<Long> uniqueSeatIds = new HashSet<>(showSeatIds);

        if (uniqueSeatIds.size() != showSeatIds.size()) {
            throw new IllegalArgumentException(
                    "Duplicate seat IDs are not allowed"
            );
        }

        // Lock selected ShowSeat rows in the database
        List<ShowSeat> seats =
                showSeatRepository.findAllByIdInForUpdate(uniqueSeatIds);

        if (seats.size() != uniqueSeatIds.size()) {
            throw new IllegalArgumentException(
                    "One or more selected seats do not exist"
            );
        }

        // Validate show and seat availability
        for (ShowSeat seat : seats) {

            if (!seat.getShow().getId().equals(showId)) {
                throw new IllegalArgumentException(
                        "Selected seat does not belong to this show"
                );
            }

            if (seat.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new IllegalStateException(
                        "Seat " + seat.getId() + " is not available"
                );
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // Create booking
        Booking booking = new Booking();

        booking.setShow(seats.get(0).getShow());
        booking.setCustomerEmail(customerEmail);
        booking.setStatus(BookingStatus.PENDING);
        booking.setHoldExpiresAt(now.plusMinutes(5));

        // Calculate total amount
        BigDecimal totalAmount = seats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        booking.setTotalAmount(totalAmount);

        Booking savedBooking =
                bookingRepository.save(booking);

        // Mark seats as HELD and link them to the booking
        for (ShowSeat seat : seats) {

            seat.setStatus(ShowSeatStatus.HELD);

            BookingSeat bookingSeat =
                    new BookingSeat(savedBooking, seat);

            bookingSeatRepository.save(bookingSeat);
        }

        return savedBooking;
    }


    // --------------------------------------------------
    // 2. Confirm booking
    // --------------------------------------------------

    @Transactional
    public Booking confirmBooking(Long bookingId) {

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found with ID: " + bookingId
                        )
                );

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking is not pending"
            );
        }

        // Reject confirmation if the 5-minute hold has expired
        if (!booking.getHoldExpiresAt()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "Seat hold has expired"
            );
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(bookingId);

        if (bookingSeats.isEmpty()) {
            throw new IllegalStateException(
                    "No seats found for this booking"
            );
        }

        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat = bookingSeat.getShowSeat();

            if (showSeat.getStatus() != ShowSeatStatus.HELD) {
                throw new IllegalStateException(
                        "One or more seats are no longer held"
                );
            }

            showSeat.setStatus(ShowSeatStatus.BOOKED);
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        return booking;
    }


    // --------------------------------------------------
    // 3. Get booking by ID
    // --------------------------------------------------

    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {

        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found with ID: " + bookingId
                        )
                );
    }
    // --------------------------------------------------
// 4. Cancel booking and request refund if applicable
// --------------------------------------------------

    @Transactional
    public Booking cancelBooking(Long bookingId) {

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Booking not found with ID: " + bookingId
                        )
                );

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only confirmed bookings can be cancelled"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime showStartTime =
                booking.getShow().getStartTime();

        // Prevent cancellation once the show has started
        if (!showStartTime.isAfter(now)) {
            throw new IllegalStateException(
                    "Cannot cancel booking after the show has started"
            );
        }

        // Cancellation cutoff: 2 hours before the show
        LocalDateTime cancellationDeadline =
                showStartTime.minusHours(2);

        if (now.isAfter(cancellationDeadline)) {
            throw new IllegalStateException(
                    "Cancellation is allowed only up to 2 hours before the show"
            );
        }

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingId(bookingId);

        if (bookingSeats.isEmpty()) {
            throw new IllegalStateException(
                    "No seats found for this booking"
            );
        }

        // Release booked seats
        for (BookingSeat bookingSeat : bookingSeats) {

            ShowSeat showSeat = bookingSeat.getShowSeat();

            if (showSeat.getStatus() != ShowSeatStatus.BOOKED) {
                throw new IllegalStateException(
                        "Seat " + showSeat.getId()
                                + " is not currently booked"
                );
            }

            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
        }

        // Mark booking as cancelled
        booking.setStatus(BookingStatus.CANCELLED);

        // Find payment for this booking
        Payment payment = paymentRepository
                .findByBookingId(bookingId)
                .orElse(null);

        // Request refund only for a successful payment
        if (payment != null
                && payment.getStatus() == PaymentStatus.SUCCESS) {

            payment.setStatus(PaymentStatus.REFUND_PENDING);
            paymentRepository.save(payment);

            try {
                refundService.processRefund(payment.getId());
            } catch (RazorpayException e) {
                throw new IllegalStateException(
                        "Unable to initiate refund with Razorpay",
                        e
                );
            }
        }

        return booking;
    }
    @Transactional(readOnly = true)
    public List<Booking> getBookingHistory(String customerEmail) {

        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email is required"
            );
        }

        return bookingRepository
                .findByCustomerEmailOrderByIdDesc(customerEmail.trim());
    }
}