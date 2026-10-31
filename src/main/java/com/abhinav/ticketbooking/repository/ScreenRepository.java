package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.movie.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatreId(Long theatreId);

    Optional<Screen> findByTheatreIdAndScreenNumber(
            Long theatreId,
            Integer screenNumber
    );
    List<Screen> findByTheatre_Id(Long theatreId);
}