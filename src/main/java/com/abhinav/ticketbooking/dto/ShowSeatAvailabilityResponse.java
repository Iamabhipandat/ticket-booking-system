package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.entity.show.ShowSeatStatus;

import java.math.BigDecimal;

public class ShowSeatAvailabilityResponse {

    private Long showSeatId;
    private Long seatId;
    private ShowSeatStatus status;
    private BigDecimal price;

    public ShowSeatAvailabilityResponse(
            Long showSeatId,
            Long seatId,
            ShowSeatStatus status,
            BigDecimal price
    ) {
        this.showSeatId = showSeatId;
        this.seatId = seatId;
        this.status = status;
        this.price = price;
    }

    public static ShowSeatAvailabilityResponse from(ShowSeat showSeat) {
        return new ShowSeatAvailabilityResponse(
                showSeat.getId(),
                showSeat.getSeat().getId(),
                showSeat.getStatus(),
                showSeat.getPrice()
        );
    }

    public Long getShowSeatId() {
        return showSeatId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }
}