package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.ShowDetailsResponse;
import com.abhinav.ticketbooking.entity.show.Show;
import com.abhinav.ticketbooking.service.showseat.ShowService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    // Create a show (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/movie/{movieId}/screen/{screenId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ShowDetailsResponse createShow(
            @PathVariable Long movieId,
            @PathVariable Long screenId,
            @RequestBody Show show
    ) {
        return showService.createShow(movieId, screenId, show);
    }

    // Get show details
    @GetMapping("/{id}")
    public ShowDetailsResponse getShowById(
            @PathVariable Long id
    ) {
        return showService.getShowDetails(id);
    }

    // Get shows by screen
    @GetMapping("/screen/{screenId}")
    public List<ShowDetailsResponse> getShowsByScreen(
            @PathVariable Long screenId
    ) {
        return showService.getShowsByScreen(screenId);
    }

    // Get shows by movie
    @GetMapping("/movie/{movieId}")
    public List<ShowDetailsResponse> getShowsByMovie(
            @PathVariable Long movieId
    ) {
        return showService.getShowsByMovie(movieId);
    }

    // Cancel a show (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ShowDetailsResponse cancelShow(
            @PathVariable Long id
    ) {
        return showService.cancelShow(id);
    }

    // Filter shows by movie and date
    @GetMapping("/movie/{movieId}/date")
    public List<ShowDetailsResponse> getShowsByMovieAndDate(
            @PathVariable Long movieId,
            @RequestParam LocalDate date
    ) {
        return showService.getShowsByMovieAndDate(movieId, date);
    }
}
