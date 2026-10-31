package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.service.screen.ScreenService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
public class ScreenController {

    private final ScreenService screenService;

    public ScreenController(ScreenService screenService) {
        this.screenService = screenService;
    }

    @PostMapping("/theatre/{theatreId}")
    public Screen createScreen(
            @PathVariable Long theatreId,
            @RequestBody Screen screen
    ) {
        return screenService.createScreen(theatreId, screen);
    }

    @GetMapping("/theatre/{theatreId}")
    public List<Screen> getScreensByTheatre(
            @PathVariable Long theatreId
    ) {
        return screenService.getScreensByTheatre(theatreId);
    }

    @GetMapping("/{id}")
    public Screen getScreenById(@PathVariable Long id) {
        return screenService.getScreenById(id);
    }

    @PutMapping("/{id}")
    public Screen updateScreen(
            @PathVariable Long id,
            @RequestBody Screen screen
    ) {
        return screenService.updateScreen(id, screen);
    }

    @DeleteMapping("/{id}")
    public void deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
    }
}