package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    // Find pending bookings whose 5-minute hold has expired
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = :status
          AND b.holdExpiresAt <= :now
    """)
    List<Booking> findExpiredBookings(
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );


    // Lock a booking row while confirming it
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.id = :bookingId
    """)
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId
    );
    List<Booking> findByCustomerEmailOrderByIdDesc(String customerEmail);
}