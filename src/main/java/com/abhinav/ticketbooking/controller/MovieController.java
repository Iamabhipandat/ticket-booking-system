package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.MovieResponse;
import com.abhinav.ticketbooking.entity.movie.Movie;
import com.abhinav.ticketbooking.service.movie.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    // Add a movie (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse addMovie(@RequestBody Movie movie) {
        return movieService.addMovie(movie);
    }

    // Get all movies
    @GetMapping
    public List<MovieResponse> getAllMovies() {
        return movieService.getAllMovieDetails();
    }

    // Get movie by ID
    @GetMapping("/{id}")
    public MovieResponse getMovieById(@PathVariable Long id) {
        return movieService.getMovieDetails(id);
    }

    // Delete movie (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(Map.of("message", "Movie deleted successfully"));
    }

    // Search movies by title
    @GetMapping("/search")
    public List<MovieResponse> searchMovies(@RequestParam String title) {
        return movieService.searchMovies(title);
    }

    // Filter movies by language
    @GetMapping("/language/{language}")
    public List<MovieResponse> getMoviesByLanguage(@PathVariable String language) {
        return movieService.getMoviesByLanguage(language);
    }

    // Filter movies by genre
    @GetMapping("/genre/{genre}")
    public List<MovieResponse> getMoviesByGenre(@PathVariable String genre) {
        return movieService.getMoviesByGenre(genre);
    }

    // Filter movies by language and genre
    @GetMapping("/filter")
    public List<MovieResponse> getMoviesByLanguageAndGenre(
            @RequestParam String language,
            @RequestParam String genre
    ) {
        return movieService.getMoviesByLanguageAndGenre(language, genre);
    }
}
