package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.TheatreResponse;
import com.abhinav.ticketbooking.entity.movie.Theatre;
import com.abhinav.ticketbooking.service.theatre.TheatreService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theatres")
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    // Add a theatre (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TheatreResponse addTheatre(@RequestBody Theatre theatre) {
        return theatreService.addTheatre(theatre);
    }

    // Get theatre by ID
    @GetMapping("/{id}")
    public TheatreResponse getTheatreById(@PathVariable Long id) {
        return theatreService.getTheatreById(id);
    }

    // Find theatres by city
    @GetMapping("/city/{city}")
    public List<TheatreResponse> getTheatresByCity(
            @PathVariable String city
    ) {
        return theatreService.getTheatresByCity(city);
    }

    // Search theatres by name
    @GetMapping("/search")
    public List<TheatreResponse> searchTheatresByName(
            @RequestParam String name
    ) {
        return theatreService.searchTheatresByName(name);
    }

    // Filter theatres by city and name
    @GetMapping("/filter")
    public List<TheatreResponse> getTheatresByCityAndName(
            @RequestParam String city,
            @RequestParam String name
    ) {
        return theatreService.getTheatresByCityAndName(city, name);
    }
}
