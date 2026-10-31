package com.abhinav.ticketbooking.service.showseat;

import com.abhinav.ticketbooking.dto.ShowDetailsResponse;
import com.abhinav.ticketbooking.entity.movie.Movie;
import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.show.Show;
import com.abhinav.ticketbooking.entity.show.ShowStatus;
import com.abhinav.ticketbooking.repository.MovieRepository;
import com.abhinav.ticketbooking.repository.ScreenRepository;
import com.abhinav.ticketbooking.repository.ShowRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowService(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository
    ) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    // Create a new show
    @Transactional
    public Show createShow(
            Long movieId,
            Long screenId,
            Show show
    ) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() ->
                        new RuntimeException("Movie not found")
                );

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found")
                );

        if (show.getStartTime() == null
                || show.getEndTime() == null) {

            throw new RuntimeException(
                    "Show start time and end time are required"
            );
        }

        if (!show.getEndTime().isAfter(show.getStartTime())) {
            throw new RuntimeException(
                    "Show end time must be after start time"
            );
        }

        if (show.getTicketPrice() == null
                || show.getTicketPrice().signum() <= 0) {

            throw new RuntimeException(
                    "Ticket price must be greater than zero"
            );
        }

        boolean overlappingShow =
                showRepository.existsOverlappingShow(
                        screenId,
                        show.getStartTime(),
                        show.getEndTime(),
                        ShowStatus.SCHEDULED
                );

        if (overlappingShow) {
            throw new RuntimeException(
                    "Another show is already scheduled during this time"
            );
        }

        show.setMovie(movie);
        show.setScreen(screen);
        show.setStatus(ShowStatus.SCHEDULED);

        return showRepository.save(show);
    }

    // Get show entity by ID
    @Transactional(readOnly = true)
    public Show getShowById(Long id) {

        return showRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Show not found")
                );
    }

    // Get show details DTO by ID
    @Transactional(readOnly = true)
    public ShowDetailsResponse getShowDetails(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new RuntimeException("Show not found")
                );

        return ShowDetailsResponse.from(show);
    }

    // Get shows by screen
    @Transactional(readOnly = true)
    public List<Show> getShowsByScreen(Long screenId) {

        if (!screenRepository.existsById(screenId)) {
            throw new RuntimeException("Screen not found");
        }

        return showRepository.findByScreenId(screenId);
    }

    // Get shows by movie
    @Transactional(readOnly = true)
    public List<Show> getShowsByMovie(Long movieId) {

        if (!movieRepository.existsById(movieId)) {
            throw new RuntimeException("Movie not found");
        }

        return showRepository.findByMovieId(movieId);
    }

    // Cancel a show
    @Transactional
    public Show cancelShow(Long id) {

        Show show = getShowById(id);

        show.setStatus(ShowStatus.CANCELLED);

        return showRepository.save(show);
    }

    public List<ShowDetailsResponse> getShowsByMovieAndDate(
            Long movieId,
            LocalDate date
    ) {
        if (!movieRepository.existsById(movieId)) {
            throw new RuntimeException("Movie not found");
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime startOfNextDay = date.plusDays(1).atStartOfDay();

        return showRepository
                .findByMovie_IdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatus(
                        movieId,
                        startOfDay,
                        startOfNextDay,
                        ShowStatus.SCHEDULED
                )
                .stream()
                .map(ShowDetailsResponse::from)
                .toList();
    }
}