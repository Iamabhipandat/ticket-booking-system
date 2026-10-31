package com.abhinav.ticketbooking.service.showseat;

import com.abhinav.ticketbooking.dto.SeatLayoutResponse;
import com.abhinav.ticketbooking.dto.SeatRowResponse;
import com.abhinav.ticketbooking.entity.seat.Seat;
import com.abhinav.ticketbooking.entity.seat.SeatType;
import com.abhinav.ticketbooking.entity.show.Show;
import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.entity.show.ShowSeatStatus;
import com.abhinav.ticketbooking.repository.SeatRepository;
import com.abhinav.ticketbooking.repository.ShowRepository;
import com.abhinav.ticketbooking.repository.ShowSeatRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ShowSeatService {

    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    public ShowSeatService(
            ShowSeatRepository showSeatRepository,
            ShowRepository showRepository,
            SeatRepository seatRepository
    ) {
        this.showSeatRepository = showSeatRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
    }

    // Initialize seats for a show
    @Transactional
    public List<ShowSeat> initializeShowSeats(Long showId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                        new RuntimeException("Show not found"));

        List<Seat> seats =
                seatRepository.findByScreenId(show.getScreen().getId());

        if (seats.isEmpty()) {
            throw new RuntimeException(
                    "No seats are configured for this screen"
            );
        }

        List<ShowSeat> showSeats = new ArrayList<>();

        for (Seat seat : seats) {

            boolean alreadyInitialized =
                    showSeatRepository.existsByShowIdAndSeatId(
                            showId,
                            seat.getId()
                    );

            if (!alreadyInitialized) {

                ShowSeat showSeat = new ShowSeat();

                showSeat.setShow(show);
                showSeat.setSeat(seat);
                showSeat.setStatus(ShowSeatStatus.AVAILABLE);

                showSeat.setPrice(
                        calculateSeatPrice(
                                show.getTicketPrice(),
                                seat.getSeatType()
                        )
                );

                showSeats.add(showSeat);
            }
        }

        return showSeatRepository.saveAll(showSeats);
    }

    // Get all seats for a show
    @Transactional(readOnly = true)
    public List<ShowSeat> getSeatsForShow(Long showId) {

        if (!showRepository.existsById(showId)) {
            throw new RuntimeException("Show not found");
        }

        return showSeatRepository.findByShowId(showId);
    }

    // Get available seats for a show
    @Transactional(readOnly = true)
    public List<ShowSeat> getAvailableSeats(Long showId) {

        if (!showRepository.existsById(showId)) {
            throw new RuntimeException("Show not found");
        }

        return showSeatRepository.findByShowIdAndStatus(
                showId,
                ShowSeatStatus.AVAILABLE
        );
    }

    // Get seat layout grouped by rows
    @Transactional(readOnly = true)
    public List<SeatRowResponse> getSeatLayout(Long showId) {

        if (!showRepository.existsById(showId)) {
            throw new RuntimeException("Show not found");
        }

        List<ShowSeat> showSeats =
                showSeatRepository.findByShowId(showId);

        if (showSeats.isEmpty()) {
            throw new RuntimeException(
                    "Seat layout is not initialized for this show"
            );
        }

        // Sort by row name and seat number
        showSeats.sort(
                Comparator
                        .comparing(
                                (ShowSeat ss) ->
                                        ss.getSeat().getRowName()
                        )
                        .thenComparing(
                                ss -> ss.getSeat().getSeatNumber()
                        )
        );

        // Group seats by row
        Map<String, List<SeatLayoutResponse>> groupedSeats =
                new TreeMap<>();

        for (ShowSeat showSeat : showSeats) {

            String rowName =
                    showSeat.getSeat().getRowName();

            SeatLayoutResponse seatResponse =
                    SeatLayoutResponse.from(showSeat);

            groupedSeats
                    .computeIfAbsent(
                            rowName,
                            key -> new ArrayList<>()
                    )
                    .add(seatResponse);
        }

        // Convert grouped data into DTO response
        List<SeatRowResponse> rows = new ArrayList<>();

        for (Map.Entry<String, List<SeatLayoutResponse>> entry
                : groupedSeats.entrySet()) {

            rows.add(
                    new SeatRowResponse(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return rows;
    }

    // Calculate price according to seat category
    private BigDecimal calculateSeatPrice(
            BigDecimal basePrice,
            SeatType seatType
    ) {

        BigDecimal multiplier;

        switch (seatType) {

            case REGULAR:
                multiplier = new BigDecimal("1.00");
                break;

            case PREMIUM:
                multiplier = new BigDecimal("1.50");
                break;

            case RECLINER:
                multiplier = new BigDecimal("2.00");
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported seat type: " + seatType
                );
        }

        return basePrice
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }
}