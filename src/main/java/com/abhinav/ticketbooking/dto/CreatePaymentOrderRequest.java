package com.abhinav.ticketbooking.dto;

public class CreatePaymentOrderRequest {

    private Long bookingId;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}