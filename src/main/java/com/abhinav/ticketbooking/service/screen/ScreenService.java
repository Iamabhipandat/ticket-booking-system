package com.abhinav.ticketbooking.service.screen;

import com.abhinav.ticketbooking.dto.ScreenResponse;
import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.movie.Theatre;
import com.abhinav.ticketbooking.repository.ScreenRepository;
import com.abhinav.ticketbooking.repository.TheatreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;

    public ScreenService(
            ScreenRepository screenRepository,
            TheatreRepository theatreRepository
    ) {
        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
    }

    // Create a screen inside a theatre
    @Transactional
    public Screen createScreen(Long theatreId, Screen screen) {

        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() ->
                        new RuntimeException("Theatre not found"));

        boolean alreadyExists =
                screenRepository.findByTheatreIdAndScreenNumber(
                        theatreId,
                        screen.getScreenNumber()
                ).isPresent();

        if (alreadyExists) {
            throw new RuntimeException(
                    "Screen number already exists in this theatre");
        }

        screen.setTheatre(theatre);

        return screenRepository.save(screen);
    }

    // Get all screens belonging to a theatre
    @Transactional(readOnly = true)
    public List<Screen> getScreensByTheatre(Long theatreId) {

        if (!theatreRepository.existsById(theatreId)) {
            throw new RuntimeException("Theatre not found");
        }

        return screenRepository.findByTheatreId(theatreId);
    }

    // Get screen entity by ID
    @Transactional(readOnly = true)
    public Screen getScreenById(Long id) {

        return screenRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));
    }

    // Get screen details as DTO
    @Transactional(readOnly = true)
    public ScreenResponse getScreenDetails(Long id) {

        Screen screen = screenRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

        return ScreenResponse.from(screen);
    }

    // Get screen details for all screens in a theatre
    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreenDetailsByTheatre(
            Long theatreId
    ) {

        if (!theatreRepository.existsById(theatreId)) {
            throw new RuntimeException("Theatre not found");
        }

        return screenRepository.findByTheatreId(theatreId)
                .stream()
                .map(ScreenResponse::from)
                .toList();
    }

    // Update screen details
    @Transactional
    public Screen updateScreen(
            Long id,
            Screen updatedScreen
    ) {

        Screen existingScreen = getScreenById(id);

        existingScreen.setScreenNumber(
                updatedScreen.getScreenNumber()
        );

        existingScreen.setTotalSeats(
                updatedScreen.getTotalSeats()
        );

        return screenRepository.save(existingScreen);
    }

    // Delete a screen
    @Transactional
    public void deleteScreen(Long id) {

        Screen screen = getScreenById(id);

        screenRepository.delete(screen);
    }
}