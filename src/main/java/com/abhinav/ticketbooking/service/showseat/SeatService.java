package com.abhinav.ticketbooking.service.showseat;

import com.abhinav.ticketbooking.dto.SeatResponse;
import com.abhinav.ticketbooking.entity.movie.Screen;
import com.abhinav.ticketbooking.entity.seat.Seat;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
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
    public SeatResponse createSeat(Long screenId, Seat seat) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + screenId));

        boolean seatExists = seatRepository.existsByScreenIdAndRowNameAndSeatNumber(
                screenId,
                seat.getRowName(),
                seat.getSeatNumber()
        );

        if (seatExists) {
            throw new BadRequestException("Seat " + seat.getRowName() + seat.getSeatNumber() + " already exists in this screen");
        }

        seat.setScreen(screen);
        Seat saved = seatRepository.save(seat);
        return SeatResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByScreen(Long screenId) {
        if (!screenRepository.existsById(screenId)) {
            throw new ResourceNotFoundException("Screen not found with ID: " + screenId);
        }

        return seatRepository.findByScreenId(screenId)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByScreenAndRow(Long screenId, String rowName) {
        return seatRepository.findByScreenIdAndRowName(screenId, rowName)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeatResponse getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + id));
        return SeatResponse.from(seat);
    }

    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with ID: " + id));
        seatRepository.delete(seat);
    }
}
