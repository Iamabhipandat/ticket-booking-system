package com.abhinav.ticketbooking.service.theatre;

import com.abhinav.ticketbooking.dto.TheatreResponse;
import com.abhinav.ticketbooking.entity.movie.Theatre;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @Transactional(readOnly = true)
    public List<TheatreResponse> getTheatresByCity(String city) {
        return theatreRepository
                .findByCityIgnoreCase(city)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TheatreResponse> searchTheatresByName(String name) {
        return theatreRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TheatreResponse> getTheatresByCityAndName(String city, String name) {
        return theatreRepository
                .findByCityIgnoreCaseAndNameContainingIgnoreCase(city, name)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TheatreResponse getTheatreById(Long id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + id));

        return TheatreResponse.from(theatre);
    }

    @Transactional
    public TheatreResponse addTheatre(Theatre theatre) {
        Theatre saved = theatreRepository.save(theatre);
        return TheatreResponse.from(saved);
    }
}
