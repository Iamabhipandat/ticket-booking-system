package com.abhinav.ticketbooking.service.Booking;

import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingSeat;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.show.ShowSeat;
import com.abhinav.ticketbooking.entity.show.ShowSeatStatus;
import com.abhinav.ticketbooking.repository.BookingRepository;
import com.abhinav.ticketbooking.repository.BookingSeatRepository;
import com.abhinav.ticketbooking.repository.ShowSeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;

    public BookingExpiryScheduler(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            ShowSeatRepository showSeatRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showSeatRepository = showSeatRepository;
    }

    /**
     * Periodically release seats held by abandoned/expired pending bookings every 15 seconds.
     */
    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void cleanupExpiredSeatHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.PENDING, now);

        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {
            log.info("Expiring booking #{} - hold expired at {}", booking.getId(), booking.getHoldExpiresAt());
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);

            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
            for (BookingSeat bookingSeat : bookingSeats) {
                ShowSeat showSeat = bookingSeat.getShowSeat();
                if (showSeat.getStatus() == ShowSeatStatus.HELD) {
                    showSeat.setStatus(ShowSeatStatus.AVAILABLE);
                    showSeatRepository.save(showSeat);
                }
            }

            log.info("Released {} seats for expired booking #{}", bookingSeats.size(), booking.getId());
        }
    }
}
