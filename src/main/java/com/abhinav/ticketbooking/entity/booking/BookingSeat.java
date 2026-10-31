package com.abhinav.ticketbooking.entity.booking;

import com.abhinav.ticketbooking.entity.show.ShowSeat;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "booking_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_show_seat",
                        columnNames = {"booking_id", "show_seat_id"}
                )
        }
)
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    // Seat ki price at the time of booking
    @Column(name = "price_at_booking", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtBooking;

    public BookingSeat() {
    }

    public BookingSeat(Booking booking, ShowSeat showSeat) {
        this.booking = booking;
        this.showSeat = showSeat;
        this.priceAtBooking = showSeat.getPrice();
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public ShowSeat getShowSeat() {
        return showSeat;
    }

    public BigDecimal getPriceAtBooking() {
        return priceAtBooking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setShowSeat(ShowSeat showSeat) {
        this.showSeat = showSeat;
    }

    public void setPriceAtBooking(BigDecimal priceAtBooking) {
        this.priceAtBooking = priceAtBooking;
    }
}