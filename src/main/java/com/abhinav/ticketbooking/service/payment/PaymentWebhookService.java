package com.abhinav.ticketbooking.service.payment;

import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.payment.Payment;
import com.abhinav.ticketbooking.entity.payment.RazorpayWebhookEvent;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.payment.PaymentStatus;
import com.abhinav.ticketbooking.entity.payment.RefundStatus;

import com.abhinav.ticketbooking.repository.PaymentRepository;
import com.abhinav.ticketbooking.repository.RazorpayWebhookEventRepository;

import com.abhinav.ticketbooking.service.Booking.BookingService;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final RazorpayWebhookEventRepository eventRepository;
    private final BookingService bookingService;

    public PaymentWebhookService(
            PaymentRepository paymentRepository,
            RazorpayWebhookEventRepository eventRepository,
            BookingService bookingService
    ) {
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.bookingService = bookingService;
    }

    @Transactional
    public void processEvent(String eventId, String rawBody) {

        // Ignore duplicate events.
        if (eventRepository.existsByEventId(eventId)) {
            return;
        }

        JSONObject event = new JSONObject(rawBody);
        String eventType = event.optString("event");

        /*
         * Handle refund.processed.
         */
        if (eventType.equals("refund.processed")) {
            processRefundProcessedEvent(event, eventId);
            return;
        }

        /*
         * Record events that are not currently supported.
         */
        if (!eventType.equals("payment.captured")
                && !eventType.equals("payment.failed")) {

            eventRepository.save(
                    new RazorpayWebhookEvent(eventId, eventType)
            );

            return;
        }

        JSONObject paymentEntity = event
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String paymentId = paymentEntity.optString("id");
        String orderId = paymentEntity.optString("order_id");

        if (paymentId.isBlank() || orderId.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment ID or order ID is missing"
            );
        }

        // Lock the payment record to avoid concurrent updates.
        Payment payment = paymentRepository
                .findByRazorpayOrderIdForUpdate(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment record not found"
                        )
                );

        // Check again after acquiring the lock.
        if (eventRepository.existsByEventId(eventId)) {
            return;
        }

        // Do not overwrite completed payment states.
        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.REFUND_PENDING
                || payment.getStatus() == PaymentStatus.REFUNDED) {

            eventRepository.save(
                    new RazorpayWebhookEvent(eventId, eventType)
            );

            return;
        }

        /*
         * Handle payment.failed.
         */
        if (eventType.equals("payment.failed")) {

            payment.setRazorpayPaymentId(paymentId);
            payment.setStatus(PaymentStatus.FAILED);

            paymentRepository.save(payment);
        }

        /*
         * Handle payment.captured.
         */
        else if (eventType.equals("payment.captured")) {

            long receivedAmountPaise =
                    paymentEntity.getLong("amount");

            long expectedAmountPaise = payment.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            // Validate captured amount.
            if (receivedAmountPaise != expectedAmountPaise) {
                throw new IllegalArgumentException(
                        "Captured amount does not match payment amount"
                );
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

                // Payment succeeded; confirm the booking.
                payment.setStatus(PaymentStatus.SUCCESS);

                paymentRepository.save(payment);

                bookingService.confirmBooking(booking.getId());

            } else {

                // Payment captured after booking hold expiry.
                // Refund processing is required.
                payment.setStatus(PaymentStatus.REFUND_PENDING);
                payment.setRefundStatus(RefundStatus.PENDING);

                paymentRepository.save(payment);
            }
        }

        // Save event only after database processing succeeds.
        eventRepository.save(
                new RazorpayWebhookEvent(eventId, eventType)
        );
    }

    /*
     * Handle refund.processed.
     */
    private void processRefundProcessedEvent(
            JSONObject event,
            String eventId
    ) {

        JSONObject refundEntity = event
                .getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity");

        String refundId = refundEntity.optString("id");

        if (refundId.isBlank()) {
            throw new IllegalArgumentException(
                    "Razorpay refund ID is missing"
            );
        }

        // Find the payment associated with the refund.
        Payment payment = paymentRepository
                .findByRazorpayRefundIdForUpdate(refundId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment for this refund was not found"
                        )
                );

        // Check again after acquiring the lock.
        if (eventRepository.existsByEventId(eventId)) {
            return;
        }

        // Mark refund and payment as completed.
        payment.setRefundStatus(RefundStatus.PROCESSED);
        payment.setStatus(PaymentStatus.REFUNDED);

        paymentRepository.save(payment);

        eventRepository.save(
                new RazorpayWebhookEvent(
                        eventId,
                        "refund.processed"
                )
        );
    }
}