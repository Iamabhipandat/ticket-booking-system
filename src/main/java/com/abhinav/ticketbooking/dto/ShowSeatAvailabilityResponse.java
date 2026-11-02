package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.entity.show.ShowSeatStatus;

import java.math.BigDecimal;

public class ShowSeatAvailabilityResponse {

    private Long showSeatId;
    private Long seatId;
    private String rowName;
    private Integer seatNumber;
    private String seatType;
    private ShowSeatStatus status;
    private BigDecimal price;

    public ShowSeatAvailabilityResponse() {
    }

    public ShowSeatAvailabilityResponse(
            Long showSeatId,
            Long seatId,
            String rowName,
            Integer seatNumber,
            String seatType,
            ShowSeatStatus status,
            BigDecimal price
    ) {
        this.showSeatId = showSeatId;
        this.seatId = seatId;
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = status;
        this.price = price;
    }

    public static ShowSeatAvailabilityResponse from(ShowSeat showSeat) {
        var seat = showSeat.getSeat();
        return new ShowSeatAvailabilityResponse(
                showSeat.getId(),
                seat.getId(),
                seat.getRowName(),
                seat.getSeatNumber(),
                seat.getSeatType().name(),
                showSeat.getStatus(),
                showSeat.getPrice()
        );
    }

    public Long getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(Long showSeatId) {
        this.showSeatId = showSeatId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
