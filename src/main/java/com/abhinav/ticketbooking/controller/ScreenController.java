package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.ScreenResponse;
import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.service.screen.ScreenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/screens")
public class ScreenController {

    private final ScreenService screenService;

    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    // Create a screen inside a theatre (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/theatre/{theatreId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreenResponse createScreen(
            @PathVariable Long theatreId,
            @RequestBody Screen screen
    ) {
        return screenService.createScreen(theatreId, screen);
    }

    // Get all screens belonging to a theatre
    @GetMapping("/theatre/{theatreId}")
    public List<ScreenResponse> getScreensByTheatre(
            @PathVariable Long theatreId
    ) {
        return screenService.getScreensByTheatre(theatreId);
    }

    // Get screen details by ID
    @GetMapping("/{id}")
    public ScreenResponse getScreenById(@PathVariable Long id) {
        return screenService.getScreenDetails(id);
    }

    // Update screen details (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ScreenResponse updateScreen(
            @PathVariable Long id,
            @RequestBody Screen screen
    ) {
        return screenService.updateScreen(id, screen);
    }

    // Delete screen (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.ok(Map.of("message", "Screen deleted successfully"));
    }
}
