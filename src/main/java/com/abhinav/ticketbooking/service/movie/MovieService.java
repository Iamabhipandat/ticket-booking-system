package com.abhinav.ticketbooking.service.movie;

import com.abhinav.ticketbooking.dto.MovieResponse;
import com.abhinav.ticketbooking.entity.movie.Movie;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Add a new movie
    @Transactional
    public MovieResponse addMovie(Movie movie) {
        Movie saved = movieRepository.save(movie);
        return MovieResponse.from(saved);
    }

    // Get all movies as DTO responses
    @Transactional(readOnly = true)
    public List<MovieResponse> getAllMovieDetails() {
        return movieRepository.findAll()
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    // Get movie details by ID as a DTO
    @Transactional(readOnly = true)
    public MovieResponse getMovieDetails(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));

        return MovieResponse.from(movie);
    }

    // Get movie by ID as an entity (internal)
    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with ID: " + id));
    }

    // Delete movie
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with ID: " + id);
        }
        movieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> searchMovies(String title) {
        return movieRepository
                .findByTitleContainingIgnoreCase(title)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByLanguage(String language) {
        return movieRepository
                .findByLanguageIgnoreCase(language)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByGenre(String genre) {
        return movieRepository
                .findByGenreIgnoreCase(genre)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> getMoviesByLanguageAndGenre(String language, String genre) {
        return movieRepository
                .findByLanguageIgnoreCaseAndGenreIgnoreCase(language, genre)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }
}
