package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.BookingResponse;
import com.abhinav.ticketbooking.dto.HoldBookingRequest;
import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingSeat;
import com.abhinav.ticketbooking.repository.BookingSeatRepository;
import com.abhinav.ticketbooking.service.Booking.BookingService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingSeatRepository bookingSeatRepository;

    public BookingController(
            BookingService bookingService,
            BookingSeatRepository bookingSeatRepository) {

        this.bookingService = bookingService;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // 1. Hold selected seats for 5 minutes
    @PostMapping("/hold")
    public BookingResponse holdSeats(
            @RequestBody HoldBookingRequest request) {

        Booking booking = bookingService.holdSeats(
                request.getShowId(),
                request.getShowSeatIds(),
                request.getCustomerEmail()
        );

        return toResponse(booking);
    }

    // 2. Confirm booking
    @PostMapping("/{bookingId}/confirm")
    public BookingResponse confirmBooking(
            @PathVariable Long bookingId) {

        Booking booking =
                bookingService.confirmBooking(bookingId);

        return toResponse(booking);
    }

    // 3. Get booking by ID
    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(
            @PathVariable Long bookingId) {

        Booking booking =
                bookingService.getBookingById(bookingId);

        return toResponse(booking);
    }

    // 4. Cancel booking and request refund if applicable
    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId) {

        Booking booking =
                bookingService.cancelBooking(bookingId);

        return toResponse(booking);
    }

    // Convert entity to response DTO
    private BookingResponse toResponse(Booking booking) {

        List<Long> seatIds =
                bookingSeatRepository.findByBookingId(booking.getId())
                        .stream()
                        .map(BookingSeat::getShowSeat)
                        .map(showSeat -> showSeat.getId())
                        .toList();

        return BookingResponse.from(booking, seatIds);
    }
    // 5. Get booking history for a customer
    @GetMapping("/customer/{customerEmail}")
    public List<BookingResponse> getBookingHistory(
            @PathVariable String customerEmail) {

        List<Booking> bookings =
                bookingService.getBookingHistory(customerEmail);

        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }
}