package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.show.ShowSeat;

import java.math.BigDecimal;

public class SeatLayoutResponse {

    private Long showSeatId;
    private Long seatId;

    private String rowName;
    private Integer seatNumber;
    private String seatType;

    private String status;
    private BigDecimal price;

    public SeatLayoutResponse(
            Long showSeatId,
            Long seatId,
            String rowName,
            Integer seatNumber,
            String seatType,
            String status,
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

    public static SeatLayoutResponse from(ShowSeat showSeat) {

        var seat = showSeat.getSeat();

        return new SeatLayoutResponse(
                showSeat.getId(),
                seat.getId(),
                seat.getRowName(),
                seat.getSeatNumber(),
                seat.getSeatType().name(),
                showSeat.getStatus().name(),
                showSeat.getPrice()
        );
    }

    public Long getShowSeatId() {
        return showSeatId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public String getRowName() {
        return rowName;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }
}