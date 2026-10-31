package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.seat.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByScreenId(Long screenId);

    List<Seat> findByScreenIdAndRowName(
            Long screenId,
            String rowName
    );

    boolean existsByScreenIdAndRowNameAndSeatNumber(
            Long screenId,
            String rowName,
            Integer seatNumber
    );
}