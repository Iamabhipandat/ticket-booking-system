package com.abhinav.ticketbooking.entity.movie;

import jakarta.persistence.*;

@Entity
@Table(
        name = "screens",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"theatre_id", "screen_number"}
                )
        }
)
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "screen_number", nullable = false)
    private Integer screenNumber;

    @Column(nullable = false)
    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    public Screen() {
    }

    public Screen(Integer screenNumber, Integer totalSeats, Theatre theatre) {
        this.screenNumber = screenNumber;
        this.totalSeats = totalSeats;
        this.theatre = theatre;
    }

    public Long getId() {
        return id;
    }

    public Integer getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(Integer screenNumber) {
        this.screenNumber = screenNumber;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public void setTheatre(Theatre theatre) {
        this.theatre = theatre;
    }
}