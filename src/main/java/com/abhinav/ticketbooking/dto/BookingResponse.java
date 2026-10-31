package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.booking.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {

    private Long bookingId;
    private Long showId;
    private String customerEmail;
    private List<Long> showSeatIds;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime holdExpiresAt;

    public BookingResponse() {
    }

    public static BookingResponse from(
            Booking booking,
            List<Long> seatIds) {

        BookingResponse response = new BookingResponse();

        response.setBookingId(booking.getId());
        response.setShowId(booking.getShow().getId());
        response.setCustomerEmail(booking.getCustomerEmail());
        response.setShowSeatIds(seatIds);
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus().name());
        response.setCreatedAt(booking.getCreatedAt());
        response.setHoldExpiresAt(booking.getHoldExpiresAt());

        return response;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public List<Long> getShowSeatIds() {
        return showSeatIds;
    }

    public void setShowSeatIds(List<Long> showSeatIds) {
        this.showSeatIds = showSeatIds;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }
}