package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.booking.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByBookingId(Long bookingId);

    Optional<Ticket> findByTicketCode(String ticketCode);
}