package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.movie.Theatre;

public class TheatreResponse {

    private Long theatreId;
    private String name;
    private String city;
    private String address;
    private Integer totalScreens;

    public TheatreResponse(
            Long theatreId,
            String name,
            String city,
            String address,
            Integer totalScreens
    ) {
        this.theatreId = theatreId;
        this.name = name;
        this.city = city;
        this.address = address;
        this.totalScreens = totalScreens;
    }

    public static TheatreResponse from(Theatre theatre) {
        return new TheatreResponse(
                theatre.getId(),
                theatre.getName(),
                theatre.getCity(),
                theatre.getAddress(),
                theatre.getScreens().size()
        );
    }

    public Long getTheatreId() {
        return theatreId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public Integer getTotalScreens() {
        return totalScreens;
    }
}