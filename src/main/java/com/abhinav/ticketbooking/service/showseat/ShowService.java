package com.abhinav.ticketbooking.service.showseat;

import com.abhinav.ticketbooking.dto.ShowDetailsResponse;
import com.abhinav.ticketbooking.entity.movie.Movie;
import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.show.Show;
import com.abhinav.ticketbooking.entity.show.ShowStatus;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.MovieRepository;
import com.abhinav.ticketbooking.repository.ScreenRepository;
import com.abhinav.ticketbooking.repository.ShowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowService.class);

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final ShowSeatService showSeatService;

    public ShowService(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository,
            @Lazy ShowSeatService showSeatService
    ) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.showSeatService = showSeatService;
    }

    // Create a new show and auto-initialize seats
    @Transactional
    public ShowDetailsResponse createShow(Long movieId, Long screenId, Show show) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + movieId));

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + screenId));

        if (show.getStartTime() == null || show.getEndTime() == null) {
            throw new BadRequestException("Show start time and end time are required");
        }

        if (!show.getEndTime().isAfter(show.getStartTime())) {
            throw new BadRequestException("Show end time must be after start time");
        }

        if (show.getTicketPrice() == null || show.getTicketPrice().signum() <= 0) {
            throw new BadRequestException("Ticket price must be greater than zero");
        }

        boolean overlappingShow = showRepository.existsOverlappingShow(
                screenId,
                show.getStartTime(),
                show.getEndTime(),
                ShowStatus.SCHEDULED
        );

        if (overlappingShow) {
            throw new BadRequestException("Another show is already scheduled in this screen during this time window");
        }

        show.setMovie(movie);
        show.setScreen(screen);
        show.setStatus(ShowStatus.SCHEDULED);

        Show savedShow = showRepository.save(show);

        // Auto-initialize seats for this show so it is immediately bookable
        try {
            showSeatService.initializeShowSeats(savedShow.getId());
            log.info("Auto-initialized seats for newly created show #{}", savedShow.getId());
        } catch (Exception e) {
            log.warn("Could not auto-initialize seats for show #{}: {}", savedShow.getId(), e.getMessage());
        }

        return ShowDetailsResponse.from(savedShow);
    }

    // Get show entity by ID
    @Transactional(readOnly = true)
    public Show getShowById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + id));
    }

    // Get show details DTO by ID
    @Transactional(readOnly = true)
    public ShowDetailsResponse getShowDetails(Long showId) {
        Show show = getShowById(showId);
        return ShowDetailsResponse.from(show);
    }

    // Get shows by screen
    @Transactional(readOnly = true)
    public List<ShowDetailsResponse> getShowsByScreen(Long screenId) {
        if (!screenRepository.existsById(screenId)) {
            throw new ResourceNotFoundException("Screen not found with ID: " + screenId);
        }

        return showRepository.findByScreenId(screenId)
                .stream()
                .map(ShowDetailsResponse::from)
                .toList();
    }

    // Get shows by movie
    @Transactional(readOnly = true)
    public List<ShowDetailsResponse> getShowsByMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with ID: " + movieId);
        }

        return showRepository.findByMovieId(movieId)
                .stream()
                .map(ShowDetailsResponse::from)
                .toList();
    }

    // Cancel a show
    @Transactional
    public ShowDetailsResponse cancelShow(Long id) {
        Show show = getShowById(id);
        show.setStatus(ShowStatus.CANCELLED);
        Show saved = showRepository.save(show);
        return ShowDetailsResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ShowDetailsResponse> getShowsByMovieAndDate(Long movieId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found with ID: " + movieId);
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
