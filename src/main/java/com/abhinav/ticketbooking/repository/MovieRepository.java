package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Search movies by title, ignoring uppercase/lowercase
    List<Movie> findByTitleContainingIgnoreCase(String title);

    // Find movies by language
    List<Movie> findByLanguageIgnoreCase(String language);

    // Find movies by genre
    List<Movie> findByGenreIgnoreCase(String genre);

    // Find movies by language and genre
    List<Movie> findByLanguageIgnoreCaseAndGenreIgnoreCase(
            String language,
            String genre
    );
}