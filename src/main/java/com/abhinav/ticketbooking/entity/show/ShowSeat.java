package com.abhinav.ticketbooking.entity.show;

import com.abhinav.ticketbooking.entity.seat.Seat;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"show_id", "seat_id"}
                )
        }
)
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public ShowSeat() {
    }

    public ShowSeat(
            Show show,
            Seat seat,
            ShowSeatStatus status,
            BigDecimal price
    ) {
        this.show = show;
        this.seat = seat;
        this.status = status;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}