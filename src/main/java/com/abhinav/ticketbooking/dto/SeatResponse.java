package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.seat.Seat;

public class SeatResponse {

    private Long id;
    private String rowName;
    private Integer seatNumber;
    private String seatType;
    private Long screenId;

    public SeatResponse() {
    }

    public SeatResponse(Long id, String rowName, Integer seatNumber, String seatType, Long screenId) {
        this.id = id;
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.screenId = screenId;
    }

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowName(),
                seat.getSeatNumber(),
                seat.getSeatType().name(),
                seat.getScreen().getId()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getScreenId() {
        return screenId;
    }

    public void setScreenId(Long screenId) {
        this.screenId = screenId;
    }
}
