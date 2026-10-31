package com.abhinav.ticketbooking.entity.booking;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_booking",
                        columnNames = "booking_id"
                ),
                @UniqueConstraint(
                        name = "uk_ticket_code",
                        columnNames = "ticket_code"
                )
        }
)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "ticket_code", nullable = false, unique = true)
    private String ticketCode;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private boolean used = false;

    public Ticket() {
    }

    public Ticket(Booking booking) {
        this.booking = booking;
        this.ticketCode = UUID.randomUUID().toString();
        this.issuedAt = LocalDateTime.now();
        this.used = false;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}