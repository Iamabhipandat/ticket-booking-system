package com.abhinav.ticketbooking.dto;

import com.abhinav.ticketbooking.entity.booking.Ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TicketResponse {

    private Long ticketId;
    private String ticketCode;
    private Long bookingId;
    private Long showId;
    private String movieTitle;
    private Integer screenNumber;
    private String theatreName;
    private String theatreCity;
    private List<String> seatNumbers;
    private LocalDateTime showStartTime;
    private BigDecimal totalAmount;
    private boolean used;
    private LocalDateTime issuedAt;
    private String qrCodeUrl;

    public TicketResponse() {
    }

    public static TicketResponse from(Ticket ticket, List<String> seats) {
        var booking = ticket.getBooking();
        var show = booking.getShow();
        var movie = show.getMovie();
        var screen = show.getScreen();
        var theatre = screen.getTheatre();

        TicketResponse res = new TicketResponse();
        res.setTicketId(ticket.getId());
        res.setTicketCode(ticket.getTicketCode());
        res.setBookingId(booking.getId());
        res.setShowId(show.getId());
        res.setMovieTitle(movie.getTitle());
        res.setScreenNumber(screen.getScreenNumber());
        res.setTheatreName(theatre.getName());
        res.setTheatreCity(theatre.getCity());
        res.setSeatNumbers(seats);
        res.setShowStartTime(show.getStartTime());
        res.setTotalAmount(booking.getTotalAmount());
        res.setUsed(ticket.isUsed());
        res.setIssuedAt(ticket.getIssuedAt());
        res.setQrCodeUrl("/api/tickets/" + ticket.getTicketCode() + "/qr");
        return res;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Integer getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(Integer screenNumber) {
        this.screenNumber = screenNumber;
    }

    public String getTheatreName() {
        return theatreName;
    }

    public void setTheatreName(String theatreName) {
        this.theatreName = theatreName;
    }

    public String getTheatreCity() {
        return theatreCity;
    }

    public void setTheatreCity(String theatreCity) {
        this.theatreCity = theatreCity;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
