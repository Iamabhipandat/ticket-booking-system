package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.movie.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    // Find theatres by city
    List<Theatre> findByCityIgnoreCase(String city);

    // Search theatres by name
    List<Theatre> findByNameContainingIgnoreCase(String name);

    // Find theatres by city and name
    List<Theatre> findByCityIgnoreCaseAndNameContainingIgnoreCase(
            String city,
            String name
    );
}