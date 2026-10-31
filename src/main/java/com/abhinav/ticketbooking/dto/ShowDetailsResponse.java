package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.show.Show;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowDetailsResponse {

    private Long showId;

    private Long movieId;

    private Long screenId;
    private Integer screenNumber;

    private Long theatreId;
    private String theatreName;
    private String theatreCity;
    private String theatreAddress;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal ticketPrice;
    private String status;

    public ShowDetailsResponse(
            Long showId,
            Long movieId,
            Long screenId,
            Integer screenNumber,
            Long theatreId,
            String theatreName,
            String theatreCity,
            String theatreAddress,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal ticketPrice,
            String status
    ) {
        this.showId = showId;
        this.movieId = movieId;
        this.screenId = screenId;
        this.screenNumber = screenNumber;
        this.theatreId = theatreId;
        this.theatreName = theatreName;
        this.theatreCity = theatreCity;
        this.theatreAddress = theatreAddress;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ticketPrice = ticketPrice;
        this.status = status;
    }

    public static ShowDetailsResponse from(Show show) {

        var screen = show.getScreen();
        var theatre = screen.getTheatre();

        return new ShowDetailsResponse(
                show.getId(),
                show.getMovie().getId(),

                screen.getId(),
                screen.getScreenNumber(),

                theatre.getId(),
                theatre.getName(),
                theatre.getCity(),
                theatre.getAddress(),

                show.getStartTime(),
                show.getEndTime(),

                show.getTicketPrice(),
                show.getStatus().name()
        );
    }

    public Long getShowId() {
        return showId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getScreenId() {
        return screenId;
    }

    public Integer getScreenNumber() {
        return screenNumber;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public String getStatus() {
        return status;
    }
}