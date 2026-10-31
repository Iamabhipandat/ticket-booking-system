package com.abhinav.ticketbooking.entity.payment;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "razorpay_webhook_events")
public class RazorpayWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    protected RazorpayWebhookEvent() {
    }

    public RazorpayWebhookEvent(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.receivedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}