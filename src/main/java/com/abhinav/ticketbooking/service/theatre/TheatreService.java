package com.abhinav.ticketbooking.service.theatre;

import com.abhinav.ticketbooking.dto.TheatreResponse;
import com.abhinav.ticketbooking.entity.movie.Theatre;
import com.abhinav.ticketbooking.repository.TheatreRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    public List<TheatreResponse> getTheatresByCity(String city) {
        return theatreRepository
                .findByCityIgnoreCase(city)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    public List<TheatreResponse> searchTheatresByName(String name) {
        return theatreRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    public List<TheatreResponse> getTheatresByCityAndName(
            String city,
            String name
    ) {
        return theatreRepository
                .findByCityIgnoreCaseAndNameContainingIgnoreCase(city, name)
                .stream()
                .map(TheatreResponse::from)
                .toList();
    }

    public TheatreResponse getTheatreById(Long id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theatre not found"));

        return TheatreResponse.from(theatre);
    }

    public Theatre addTheatre(Theatre theatre) {
        return theatreRepository.save(theatre);
    }
}