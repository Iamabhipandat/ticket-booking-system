package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.SeatResponse;
import com.abhinav.ticketbooking.entity.seat.Seat;
import com.abhinav.ticketbooking.service.showseat.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    // Add a seat to a screen (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/screen/{screenId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatResponse createSeat(
            @PathVariable Long screenId,
            @RequestBody Seat seat
    ) {
        return seatService.createSeat(screenId, seat);
    }

    // Get all seats for a screen
    @GetMapping("/screen/{screenId}")
    public List<SeatResponse> getSeatsByScreen(
            @PathVariable Long screenId
    ) {
        return seatService.getSeatsByScreen(screenId);
    }

    // Get seats by row
    @GetMapping("/screen/{screenId}/row/{rowName}")
    public List<SeatResponse> getSeatsByScreenAndRow(
            @PathVariable Long screenId,
            @PathVariable String rowName
    ) {
        return seatService.getSeatsByScreenAndRow(screenId, rowName);
    }

    // Get seat by ID
    @GetMapping("/{id}")
    public SeatResponse getSeatById(@PathVariable Long id) {
        return seatService.getSeatById(id);
    }

    // Delete a seat (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok(Map.of("message", "Seat deleted successfully"));
    }
}
