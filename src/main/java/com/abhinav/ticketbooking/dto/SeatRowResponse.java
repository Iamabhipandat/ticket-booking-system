package com.abhinav.ticketbooking.dto;

import java.util.List;

public class SeatRowResponse {

    private String rowName;
    private List<SeatLayoutResponse> seats;

    public SeatRowResponse(
            String rowName,
            List<SeatLayoutResponse> seats
    ) {
        this.rowName = rowName;
        this.seats = seats;
    }

    public String getRowName() {
        return rowName;
    }

    public List<SeatLayoutResponse> getSeats() {
        return seats;
    }
}