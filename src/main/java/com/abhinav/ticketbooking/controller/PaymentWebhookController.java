package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.service.payment.PaymentWebhookService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentWebhookService paymentWebhookService;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestHeader(value = "x-razorpay-event-id", required = false) String eventId,
            @RequestBody String rawBody
    ) {
        try {
            if (eventId == null || eventId.isBlank()) {
                eventId = "evt_" + System.currentTimeMillis();
            }

            // Verify webhook signature if secret is configured
            if (webhookSecret != null && !webhookSecret.isBlank() && signature != null) {
                Utils.verifyWebhookSignature(rawBody, signature, webhookSecret);
            } else {
                log.warn("Webhook received without signature verification (webhook secret not set or signature absent)");
            }

            // Process verified webhook event
            paymentWebhookService.processEvent(eventId, rawBody);
            return ResponseEntity.ok("Webhook processed");

        } catch (RazorpayException | IllegalArgumentException e) {
            log.error("Invalid webhook request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid webhook request: " + e.getMessage());

        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Webhook processing failed");
        }
    }
}
