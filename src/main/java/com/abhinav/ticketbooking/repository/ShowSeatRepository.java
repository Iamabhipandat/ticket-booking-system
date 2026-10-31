package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.entity.show.ShowSeatStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShowSeatRepository
        extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(
            Long showId,
            ShowSeatStatus status
    );

    boolean existsByShowIdAndSeatId(
            Long showId,
            Long seatId
    );

    // Lock selected seats while booking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT ss
        FROM ShowSeat ss
        WHERE ss.id IN :ids
        ORDER BY ss.id
    """)
    List<ShowSeat> findAllByIdInForUpdate(
            @Param("ids") Collection<Long> ids
    );
}