package com.abhinav.ticketbooking.dto;

import java.math.BigDecimal;

public class PaymentOrderResponse {

    private String razorpayOrderId;
    private String razorpayKeyId;
    private int amount;
    private String currency;
    private Long bookingId;

    public PaymentOrderResponse(
            String razorpayOrderId,
            String razorpayKeyId,
            int amount,
            String currency,
            Long bookingId
    ) {
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayKeyId = razorpayKeyId;
        this.amount = amount;
        this.currency = currency;
        this.bookingId = bookingId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Long getBookingId() {
        return bookingId;
    }
}