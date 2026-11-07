package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.movie.Movie;

public class MovieResponse {

    private Long movieId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private String language;
    private String genre;
    private String posterUrl;
    private Double rating;

    public MovieResponse(
            Long movieId,
            String title,
            String description,
            Integer durationMinutes,
            String language,
            String genre,
            String posterUrl,
            Double rating
    ) {
        this.movieId = movieId;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.genre = genre;
        this.posterUrl = posterUrl;
        this.rating = rating;
    }

    // Convert Movie entity into MovieResponse DTO
    public static MovieResponse from(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getLanguage(),
                movie.getGenre(),
                movie.getPosterUrl(),
                movie.getRating()
        );
    }

    public Long getId() {
        return movieId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getLanguage() {
        return language;
    }

    public String getGenre() {
        return genre;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public Double getRating() {
        return rating;
    }
}
