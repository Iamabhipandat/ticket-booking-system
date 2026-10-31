package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.entity.seat.Seat;
import com.abhinav.ticketbooking.service.showseat.SeatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/screen/{screenId}")
    public Seat createSeat(
            @PathVariable Long screenId,
            @RequestBody Seat seat
    ) {
        return seatService.createSeat(screenId, seat);
    }

    @GetMapping("/screen/{screenId}")
    public List<Seat> getSeatsByScreen(
            @PathVariable Long screenId
    ) {
        return seatService.getSeatsByScreen(screenId);
    }

    @GetMapping("/screen/{screenId}/row/{rowName}")
    public List<Seat> getSeatsByScreenAndRow(
            @PathVariable Long screenId,
            @PathVariable String rowName
    ) {
        return seatService.getSeatsByScreenAndRow(
                screenId,
                rowName
        );
    }

    @GetMapping("/{id}")
    public Seat getSeatById(@PathVariable Long id) {
        return seatService.getSeatById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
    }
}