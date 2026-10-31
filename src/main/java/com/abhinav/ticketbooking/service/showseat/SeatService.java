package com.abhinav.ticketbooking.service.showseat;

import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.seat.Seat;
import com.abhinav.ticketbooking.repository.ScreenRepository;
import com.abhinav.ticketbooking.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    public SeatService(
            SeatRepository seatRepository,
            ScreenRepository screenRepository
    ) {
        this.seatRepository = seatRepository;
        this.screenRepository = screenRepository;
    }

    @Transactional
    public Seat createSeat(Long screenId, Seat seat) {

        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

        boolean seatExists =
                seatRepository
                        .existsByScreenIdAndRowNameAndSeatNumber(
                                screenId,
                                seat.getRowName(),
                                seat.getSeatNumber()
                        );

        if (seatExists) {
            throw new RuntimeException(
                    "Seat already exists in this screen"
            );
        }

        seat.setScreen(screen);

        return seatRepository.save(seat);
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsByScreen(Long screenId) {

        if (!screenRepository.existsById(screenId)) {
            throw new RuntimeException("Screen not found");
        }

        return seatRepository.findByScreenId(screenId);
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsByScreenAndRow(
            Long screenId,
            String rowName
    ) {
        return seatRepository.findByScreenIdAndRowName(
                screenId,
                rowName
        );
    }

    @Transactional(readOnly = true)
    public Seat getSeatById(Long id) {

        return seatRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Seat not found"));
    }

    @Transactional
    public void deleteSeat(Long id) {

        Seat seat = getSeatById(id);

        seatRepository.delete(seat);
    }
}