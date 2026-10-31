package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.entity.booking.Ticket;
import com.abhinav.ticketbooking.service.Booking.TicketService;
import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Ticket> getTicket(
            @PathVariable String ticketCode
    ) {
        Ticket ticket = ticketService.getTicketByCode(ticketCode);
        return ResponseEntity.ok(ticket);
    }

    // Download/display QR code as PNG
    @GetMapping("/{ticketCode}/qr")
    public ResponseEntity<byte[]> getTicketQrCode(
            @PathVariable String ticketCode
    ) throws WriterException, IOException {

        Ticket ticket = ticketService.getTicketByCode(ticketCode);

        byte[] qrCode = ticketService.generateQrCode(ticket.getId());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket-qr.png"
                )
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCode);
    }
    @PostMapping("/validate/{ticketCode}")
    public ResponseEntity<Ticket> validateTicket(
            @PathVariable String ticketCode
    ) {
        Ticket ticket = ticketService.validateTicket(ticketCode);
        return ResponseEntity.ok(ticket);
    }
}