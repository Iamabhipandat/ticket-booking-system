package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.show.Show;
import com.abhinav.ticketbooking.entity.show.ShowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByScreenId(Long screenId);

    List<Show> findByMovieId(Long movieId);

    List<Show> findByStatus(ShowStatus status);

    @Query("""
            SELECT COUNT(s) > 0
            FROM Show s
            WHERE s.screen.id = :screenId
              AND s.status = :status
              AND s.startTime < :endTime
              AND s.endTime > :startTime
            """)
    boolean existsOverlappingShow(
            @Param("screenId") Long screenId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") ShowStatus status
    );
    List<Show> findByMovie_IdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatus(
            Long movieId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            ShowStatus status
    );
}