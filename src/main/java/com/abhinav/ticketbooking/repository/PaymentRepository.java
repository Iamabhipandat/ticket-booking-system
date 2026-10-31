package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.payment.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    boolean existsByBookingId(Long bookingId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT p
       FROM Payment p
       WHERE p.razorpayOrderId = :orderId
       """)
    Optional<Payment> findByRazorpayOrderIdForUpdate(
            @Param("orderId") String orderId
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.razorpayRefundId = :refundId
        """)
    Optional<Payment> findByRazorpayRefundIdForUpdate(
            @Param("refundId") String refundId
    );



}