package com.abhinav.ticketbooking.service.movie;

import com.abhinav.ticketbooking.dto.MovieResponse;
import com.abhinav.ticketbooking.entity.movie.Movie;
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
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
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
                .orElseThrow(() ->
                        new RuntimeException("Movie not found")
                );

        return MovieResponse.from(movie);
    }

    // Existing method: Get all movies as entities
    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Existing method: Get movie by ID as an entity
    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {

        return movieRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Movie not found")
                );
    }

    // Delete movie
    @Transactional
    public void deleteMovie(Long id) {

        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found");
        }

        movieRepository.deleteById(id);
    }
    public List<MovieResponse> searchMovies(String title) {
        return movieRepository
                .findByTitleContainingIgnoreCase(title)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    public List<MovieResponse> getMoviesByLanguage(String language) {
        return movieRepository
                .findByLanguageIgnoreCase(language)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    public List<MovieResponse> getMoviesByGenre(String genre) {
        return movieRepository
                .findByGenreIgnoreCase(genre)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }

    public List<MovieResponse> getMoviesByLanguageAndGenre(
            String language,
            String genre
    ) {
        return movieRepository
                .findByLanguageIgnoreCaseAndGenreIgnoreCase(language, genre)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }
}