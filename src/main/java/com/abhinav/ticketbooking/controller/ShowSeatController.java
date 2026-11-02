package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.SeatRowResponse;
import com.abhinav.ticketbooking.dto.ShowSeatAvailabilityResponse;
import com.abhinav.ticketbooking.service.showseat.ShowSeatService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/show-seats")
public class ShowSeatController {

    private final ShowSeatService showSeatService;

    public ShowSeatController(ShowSeatService showSeatService) {
        this.showSeatService = showSeatService;
    }

    // Initialize seats for a show (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/initialize/{showId}")
    public List<ShowSeatAvailabilityResponse> initializeShowSeats(
            @PathVariable Long showId
    ) {
        return showSeatService.initializeShowSeats(showId)
                .stream()
                .map(ShowSeatAvailabilityResponse::from)
                .toList();
    }

    // Get all seats for a show
    @GetMapping("/show/{showId}")
    public List<ShowSeatAvailabilityResponse> getSeatsForShow(
            @PathVariable Long showId
    ) {
        return showSeatService.getSeatsForShow(showId)
                .stream()
                .map(ShowSeatAvailabilityResponse::from)
                .toList();
    }

    // Get only available seats for a show
    @GetMapping("/show/{showId}/available")
    public List<ShowSeatAvailabilityResponse> getAvailableSeats(
            @PathVariable Long showId
    ) {
        return showSeatService.getAvailableSeats(showId)
                .stream()
                .map(ShowSeatAvailabilityResponse::from)
                .toList();
    }

    // Get seat layout grouped by rows
    @GetMapping("/show/{showId}/layout")
    public List<SeatRowResponse> getSeatLayout(
            @PathVariable Long showId
    ) {
        return showSeatService.getSeatLayout(showId);
    }
}
