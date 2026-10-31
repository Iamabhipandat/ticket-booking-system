package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.movie.Theatre;

public class ScreenResponse {

    private Long screenId;
    private Integer screenNumber;
    private Integer totalSeats;

    private Long theatreId;
    private String theatreName;
    private String theatreCity;
    private String theatreAddress;

    public ScreenResponse(
            Long screenId,
            Integer screenNumber,
            Integer totalSeats,
            Long theatreId,
            String theatreName,
            String theatreCity,
            String theatreAddress
    ) {
        this.screenId = screenId;
        this.screenNumber = screenNumber;
        this.totalSeats = totalSeats;
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.theatreCity = theatreCity;
        this.theatreAddress = theatreAddress;
    }

    public static ScreenResponse from(Screen screen) {
        Theatre theatre = screen.getTheatre();

        return new ScreenResponse(
                screen.getId(),
                screen.getScreenNumber(),
                screen.getTotalSeats(),
                theatre.getId(),
                theatre.getName(),
                theatre.getCity(),
                theatre.getAddress()
        );
    }

    public Long getScreenId() {
        return screenId;
    }

    public Integer getScreenNumber() {
        return screenNumber;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Long getTheatreId() {
        return theatreId;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public String getTheatreCity() {
        return theatreCity;
    }

    public String getTheatreAddress() {
        return theatreAddress;
    }
}