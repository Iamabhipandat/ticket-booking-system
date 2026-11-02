package com.abhinav.ticketbooking.service.screen;

import com.abhinav.ticketbooking.dto.ScreenResponse;
import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.movie.Theatre;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
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
    public ScreenResponse createScreen(Long theatreId, Screen screen) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));

        boolean alreadyExists = screenRepository.findByTheatreIdAndScreenNumber(
                theatreId,
                screen.getScreenNumber()
        ).isPresent();

        if (alreadyExists) {
            throw new BadRequestException("Screen number " + screen.getScreenNumber() + " already exists in this theatre");
        }

        screen.setTheatre(theatre);
        Screen saved = screenRepository.save(screen);
        return ScreenResponse.from(saved);
    }

    // Get all screens belonging to a theatre as DTOs
    @Transactional(readOnly = true)
    public List<ScreenResponse> getScreensByTheatre(Long theatreId) {
        if (!theatreRepository.existsById(theatreId)) {
            throw new ResourceNotFoundException("Theatre not found with ID: " + theatreId);
        }

        return screenRepository.findByTheatreId(theatreId)
                .stream()
                .map(ScreenResponse::from)
                .toList();
    }

    // Get screen entity by ID
    @Transactional(readOnly = true)
    public Screen getScreenById(Long id) {
        return screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + id));
    }

    // Get screen details as DTO
    @Transactional(readOnly = true)
    public ScreenResponse getScreenDetails(Long id) {
        Screen screen = getScreenById(id);
        return ScreenResponse.from(screen);
    }

    // Update screen details
    @Transactional
    public ScreenResponse updateScreen(Long id, Screen updatedScreen) {
        Screen existingScreen = getScreenById(id);

        if (updatedScreen.getScreenNumber() != null) {
            existingScreen.setScreenNumber(updatedScreen.getScreenNumber());
        }

        if (updatedScreen.getTotalSeats() != null) {
            existingScreen.setTotalSeats(updatedScreen.getTotalSeats());
        }

        Screen saved = screenRepository.save(existingScreen);
        return ScreenResponse.from(saved);
    }

    // Delete a screen
    @Transactional
    public void deleteScreen(Long id) {
        Screen screen = getScreenById(id);
        screenRepository.delete(screen);
    }
}
