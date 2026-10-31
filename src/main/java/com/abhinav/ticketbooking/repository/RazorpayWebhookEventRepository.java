package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.payment.RazorpayWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RazorpayWebhookEventRepository
        extends JpaRepository<RazorpayWebhookEvent, Long> {

    boolean existsByEventId(String eventId);
}