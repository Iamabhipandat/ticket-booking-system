package com.abhinav.ticketbooking.service.payment;

import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.payment.Payment;
import com.abhinav.ticketbooking.entity.payment.PaymentStatus;
import com.abhinav.ticketbooking.entity.payment.RazorpayWebhookEvent;
import com.abhinav.ticketbooking.entity.payment.RefundStatus;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.PaymentRepository;
import com.abhinav.ticketbooking.repository.RazorpayWebhookEventRepository;
import com.abhinav.ticketbooking.service.Booking.BookingService;
import com.abhinav.ticketbooking.service.Booking.TicketService;
import com.abhinav.ticketbooking.service.refund.RefundService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookService.class);

    private final PaymentRepository paymentRepository;
    private final RazorpayWebhookEventRepository eventRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final RefundService refundService;

    public PaymentWebhookService(
            PaymentRepository paymentRepository,
            RazorpayWebhookEventRepository eventRepository,
            BookingService bookingService,
            TicketService ticketService,
            RefundService refundService
    ) {
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.bookingService = bookingService;
        this.ticketService = ticketService;
        this.refundService = refundService;
    }

    @Transactional
    public void processEvent(String eventId, String rawBody) {
        // Ignore duplicate events
        if (eventRepository.existsByEventId(eventId)) {
            log.info("Duplicate webhook event received: {}", eventId);
            return;
        }

        JSONObject event = new JSONObject(rawBody);
        String eventType = event.optString("event");

        // 1. Handle refund.processed
        if ("refund.processed".equals(eventType)) {
            processRefundProcessedEvent(event, eventId);
            return;
        }

        // 2. Ignore non-payment events
        if (!"payment.captured".equals(eventType) && !"payment.failed".equals(eventType)) {
            eventRepository.save(new RazorpayWebhookEvent(eventId, eventType));
            return;
        }

        JSONObject paymentEntity = event
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = paymentEntity.optString("id");
        String orderId = paymentEntity.optString("order_id");

        if (paymentId.isBlank() || orderId.isBlank()) {
            throw new BadRequestException("Payment ID or order ID is missing");
        }

        // Lock the payment record to avoid concurrent updates
        Payment payment = paymentRepository
                .findByRazorpayOrderIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for order: " + orderId));

        // Check again after acquiring the lock
        if (eventRepository.existsByEventId(eventId)) {
            return;
        }

        // Do not overwrite completed payment states
        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.REFUND_PENDING
                || payment.getStatus() == PaymentStatus.REFUNDED) {

            eventRepository.save(new RazorpayWebhookEvent(eventId, eventType));
            return;
        }

        // 3. Handle payment.failed
        if ("payment.failed".equals(eventType)) {
            payment.setRazorpayPaymentId(paymentId);
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        // 4. Handle payment.captured
        else if ("payment.captured".equals(eventType)) {
            long receivedAmountPaise = paymentEntity.getLong("amount");
            long expectedAmountPaise = payment.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            // Validate captured amount
            if (receivedAmountPaise != expectedAmountPaise) {
                throw new BadRequestException("Captured amount does not match payment amount");
            }

            Booking booking = payment.getBooking();
            LocalDateTime now = LocalDateTime.now();

            boolean bookingStillValid =
                    booking.getStatus() == BookingStatus.PENDING
                            && booking.getHoldExpiresAt() != null
                            && booking.getHoldExpiresAt().isAfter(now);

            payment.setRazorpayPaymentId(paymentId);
            payment.setPaidAt(now);

            if (bookingStillValid) {
                // Payment succeeded; confirm the booking and issue ticket
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);

                Booking confirmedBooking = bookingService.confirmBooking(booking.getId());
                ticketService.issueTicket(confirmedBooking.getId());
                log.info("Booking #{} confirmed and ticket issued via webhook", booking.getId());
            } else {
                // Payment captured after booking hold expiry. Trigger auto refund!
                payment.setStatus(PaymentStatus.REFUND_PENDING);
                payment.setRefundStatus(RefundStatus.PENDING);
                Payment savedPayment = paymentRepository.save(payment);

                log.warn("Payment captured for expired booking #{}. Triggering auto refund.", booking.getId());
                try {
                    refundService.processRefund(savedPayment.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-process refund for payment #{}: {}", savedPayment.getId(), e.getMessage());
                }
            }
        }

        // Save event after processing succeeds
        eventRepository.save(new RazorpayWebhookEvent(eventId, eventType));
    }

    private void processRefundProcessedEvent(JSONObject event, String eventId) {
        JSONObject refundEntity = event
                .getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity");

        String refundId = refundEntity.optString("id");

        if (refundId.isBlank()) {
            throw new BadRequestException("Razorpay refund ID is missing");
        }

        Payment payment = paymentRepository
                .findByRazorpayRefundIdForUpdate(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for refund ID: " + refundId + " not found"));

        if (eventRepository.existsByEventId(eventId)) {
            return;
        }

        payment.setRefundStatus(RefundStatus.PROCESSED);
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        eventRepository.save(new RazorpayWebhookEvent(eventId, "refund.processed"));
        log.info("Refund {} marked as processed for payment #{}", refundId, payment.getId());
    }
}
