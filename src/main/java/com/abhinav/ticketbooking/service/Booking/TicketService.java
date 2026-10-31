package com.abhinav.ticketbooking.service.Booking;

import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.booking.Ticket;
import com.abhinav.ticketbooking.repository.BookingRepository;
import com.abhinav.ticketbooking.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    public TicketService(
            TicketRepository ticketRepository,
            BookingRepository bookingRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
    }

    // Issue a ticket only after booking is confirmed
    @Transactional
    public Ticket issueTicket(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Ticket can only be issued for a confirmed booking"
            );
        }

        // Prevent creating multiple tickets for the same booking
        return ticketRepository.findByBookingId(bookingId)
                .orElseGet(() ->
                        ticketRepository.save(new Ticket(booking))
                );
    }

    // Generate QR code as PNG image bytes
    @Transactional(readOnly = true)
    public byte[] generateQrCode(Long ticketId)
            throws WriterException, IOException {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Ticket not found"));

        String ticketCode = ticket.getTicketCode();

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(
                ticketCode,
                BarcodeFormat.QR_CODE,
                300,
                300
        );

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(
                bitMatrix,
                "PNG",
                outputStream
        );

        return outputStream.toByteArray();
    }

    // Find ticket using the code contained in the QR
    @Transactional(readOnly = true)
    public Ticket getTicketByCode(String ticketCode) {

        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid ticket code"));
    }

    @Transactional
    public Ticket validateTicket(String ticketCode) {

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid ticket code"));

        if (ticket.isUsed()) {
            throw new IllegalStateException(
                    "This ticket has already been used");
        }

        ticket.setUsed(true);

        return ticketRepository.save(ticket);
    }
}