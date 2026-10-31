package com.abhinav.ticketbooking.entity.seat;

import com.abhinav.ticketbooking.entity.movie.Screen;
import jakarta.persistence.*;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "screen_id",
                                "row_name",
                                "seat_number"
                        }
                )
        }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_name", nullable = false)
    private String rowName;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    public Seat() {
    }

    public Seat(
            String rowName,
            Integer seatNumber,
            SeatType seatType,
            Screen screen
    ) {
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.screen = screen;
    }

    public Long getId() {
        return id;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        this.seatType = seatType;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }
}