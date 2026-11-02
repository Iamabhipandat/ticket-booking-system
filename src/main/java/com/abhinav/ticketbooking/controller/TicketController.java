package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.TicketResponse;
import com.abhinav.ticketbooking.service.Booking.TicketService;
import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Get ticket details using ticket code
    @GetMapping("/{ticketCode}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String ticketCode) {
        TicketResponse response = ticketService.getTicketResponseByCode(ticketCode);
        return ResponseEntity.ok(response);
    }

    // Get ticket details by booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<TicketResponse> getTicketByBookingId(@PathVariable Long bookingId) {
        TicketResponse response = ticketService.getTicketResponseByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    // Download/display QR code as PNG
    @GetMapping("/{ticketCode}/qr")
    public ResponseEntity<byte[]> getTicketQrCode(@PathVariable String ticketCode)
            throws WriterException, IOException {

        byte[] qrCode = ticketService.generateQrCodeByCode(ticketCode);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket-" + ticketCode + "-qr.png"
                )
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }

    // Validate and check-in ticket at the cinema gate (Admin/Staff only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/validate/{ticketCode}")
    public ResponseEntity<TicketResponse> validateTicket(@PathVariable String ticketCode) {
        TicketResponse response = ticketService.validateTicket(ticketCode);
        return ResponseEntity.ok(response);
    }
}
