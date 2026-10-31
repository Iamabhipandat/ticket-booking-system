package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.service.showseat.ShowSeatService;
import org.springframework.web.bind.annotation.*;
import com.abhinav.ticketbooking.dto.SeatRowResponse;

import java.util.List;

@RestController
@RequestMapping("/api/show-seats")
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    public ShowSeatController(ShowSeatService showSeatService) {
        this.showSeatService = showSeatService;
    }

    @PostMapping("/initialize/{showId}")
    public List<ShowSeat> initializeShowSeats(
            @PathVariable Long showId
    ) {
        return showSeatService.initializeShowSeats(showId);
    }

    @GetMapping("/show/{showId}")
    public List<ShowSeat> getSeatsForShow(
            @PathVariable Long showId
    ) {
        return showSeatService.getSeatsForShow(showId);
    }

    @GetMapping("/show/{showId}/available")
    public List<ShowSeat> getAvailableSeats(
            @PathVariable Long showId
    ) {
        return showSeatService.getAvailableSeats(showId);
    }
    @GetMapping("/show/{showId}/layout")
    public List<SeatRowResponse> getSeatLayout(
            @PathVariable Long showId
    ) {
        return showSeatService.getSeatLayout(showId);
    }
}