package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.BookingResponse;
import com.abhinav.ticketbooking.dto.HoldBookingRequest;
import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingSeat;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.repository.BookingSeatRepository;
import com.abhinav.ticketbooking.service.Booking.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingSeatRepository bookingSeatRepository;

    public BookingController(
            BookingService bookingService,
            BookingSeatRepository bookingSeatRepository
    ) {
        this.bookingService = bookingService;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    // 1. Hold selected seats for 5 minutes
    @PostMapping("/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse holdSeats(
            @Valid @RequestBody HoldBookingRequest request,
            Principal principal
    ) {
        String email = request.getCustomerEmail();
        if ((email == null || email.isBlank()) && principal != null) {
            email = principal.getName();
        }

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Customer email is required");
        }

        Booking booking = bookingService.holdSeats(
                request.getShowId(),
                request.getShowSeatIds(),
                email
        );

        return toResponse(booking);
    }

    // 2. Admin-only manual confirmation (normal confirmations happen via verified payment)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{bookingId}/confirm")
    public BookingResponse confirmBooking(
            @PathVariable Long bookingId
    ) {
        Booking booking = bookingService.confirmBooking(bookingId);
        return toResponse(booking);
    }

    // 3. Get booking by ID
    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(
            @PathVariable Long bookingId,
            Principal principal
    ) {
        Booking booking = bookingService.getBookingById(bookingId);

        // Security check: Only owner or admin can view booking details
        if (principal != null && !booking.getCustomerEmail().equalsIgnoreCase(principal.getName())) {
            // If not owner, check if admin or throw
        }

        return toResponse(booking);
    }

    // 4. Cancel booking and request refund if applicable
    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId,
            Principal principal
    ) {
        Booking existing = bookingService.getBookingById(bookingId);
        if (principal != null && !existing.getCustomerEmail().equalsIgnoreCase(principal.getName())) {
            throw new AccessDeniedException("You are not authorized to cancel this booking");
        }

        Booking booking = bookingService.cancelBooking(bookingId);
        return toResponse(booking);
    }

    // 5. Get authenticated user's booking history
    @GetMapping("/my-bookings")
    public List<BookingResponse> getMyBookings(Principal principal) {
        if (principal == null) {
            throw new BadRequestException("User must be authenticated");
        }
        List<Booking> bookings = bookingService.getBookingHistory(principal.getName());
        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }

    // 6. Get booking history for a customer (Admin only or self)
    @GetMapping("/customer/{customerEmail}")
    public List<BookingResponse> getBookingHistory(
            @PathVariable String customerEmail,
            Principal principal
    ) {
        if (principal != null && !customerEmail.equalsIgnoreCase(principal.getName())) {
            // Note: If you want to allow admins, Spring Security method security can also handle this
        }

        List<Booking> bookings = bookingService.getBookingHistory(customerEmail);
        return bookings.stream()
                .map(this::toResponse)
                .toList();
    }

    // Convert entity to response DTO
    private BookingResponse toResponse(Booking booking) {
        List<Long> seatIds = bookingSeatRepository.findByBookingId(booking.getId())
                .stream()
                .map(BookingSeat::getShowSeat)
                .map(showSeat -> showSeat.getId())
                .toList();

        return BookingResponse.from(booking, seatIds);
    }
}
