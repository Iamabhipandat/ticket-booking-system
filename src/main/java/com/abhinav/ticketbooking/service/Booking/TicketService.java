package com.abhinav.ticketbooking.service.Booking;

import com.abhinav.ticketbooking.dto.TicketResponse;
import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingSeat;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.booking.Ticket;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.BookingRepository;
import com.abhinav.ticketbooking.repository.BookingSeatRepository;
import com.abhinav.ticketbooking.repository.TicketRepository;
import com.abhinav.ticketbooking.service.EmailService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final EmailService emailService;

    public TicketService(
            TicketRepository ticketRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            EmailService emailService
    ) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.emailService = emailService;
    }

    // Issue a ticket only after booking is confirmed
    @Transactional
    public Ticket issueTicket(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Ticket can only be issued for a confirmed booking");
        }

        // Prevent creating multiple tickets for the same booking
        Ticket ticket = ticketRepository.findByBookingId(bookingId)
                .orElseGet(() -> ticketRepository.save(new Ticket(booking)));

        // Send email with QR code asynchronously/safely
        try {
            byte[] qrBytes = generateQrCode(ticket.getId());
            List<String> seatNames = getSeatNamesForBooking(bookingId);
            String formattedTime = booking.getShow().getStartTime()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

            emailService.sendTicketConfirmationEmail(
                    booking.getCustomerEmail(),
                    ticket.getTicketCode(),
                    qrBytes,
                    booking.getShow().getMovie().getTitle(),
                    formattedTime,
                    seatNames,
                    booking.getTotalAmount()
            );
        } catch (Exception e) {
            log.error("Could not send ticket email to {}: {}", booking.getCustomerEmail(), e.getMessage());
        }

        return ticket;
    }

    // Generate QR code as PNG image bytes
    @Transactional(readOnly = true)
    public byte[] generateQrCode(Long ticketId) throws WriterException, IOException {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        return createQrCodeBytes(ticket.getTicketCode());
    }

    public byte[] generateQrCodeByCode(String ticketCode) throws WriterException, IOException {
        return createQrCodeBytes(ticketCode);
    }

    private byte[] createQrCodeBytes(String text) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    // Find ticket using the code contained in the QR
    @Transactional(readOnly = true)
    public Ticket getTicketByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid ticket code: " + ticketCode));
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketResponseByCode(String ticketCode) {
        Ticket ticket = getTicketByCode(ticketCode);
        List<String> seats = getSeatNamesForBooking(ticket.getBooking().getId());
        return TicketResponse.from(ticket, seats);
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketResponseByBookingId(Long bookingId) {
        Ticket ticket = ticketRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No ticket issued for booking ID: " + bookingId));
        List<String> seats = getSeatNamesForBooking(bookingId);
        return TicketResponse.from(ticket, seats);
    }

    @Transactional
    public TicketResponse validateTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid ticket code"));

        if (ticket.isUsed()) {
            throw new IllegalStateException("This ticket has already been used");
        }

        ticket.setUsed(true);
        Ticket saved = ticketRepository.save(ticket);
        List<String> seats = getSeatNamesForBooking(saved.getBooking().getId());
        return TicketResponse.from(saved, seats);
    }

    private List<String> getSeatNamesForBooking(Long bookingId) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        return bookingSeats.stream()
                .map(bs -> bs.getShowSeat().getSeat().getRowName() + bs.getShowSeat().getSeat().getSeatNumber())
                .toList();
    }
}
