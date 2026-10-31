package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.service.payment.PaymentWebhookService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public PaymentWebhookController(
            PaymentWebhookService paymentWebhookService
    ) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("X-Razorpay-Signature")
            String signature,

            @RequestHeader("x-razorpay-event-id")
            String eventId,

            @RequestBody
            String rawBody
    ) {

        try {
            // Verify the original raw request body
            Utils.verifyWebhookSignature(
                    rawBody,
                    signature,
                    webhookSecret
            );

            // Process only after signature verification
            paymentWebhookService.processEvent(
                    eventId,
                    rawBody
            );

            return ResponseEntity.ok("Webhook processed");

        } catch (RazorpayException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid webhook request");

        } catch (Exception e) {
            // Return a failure response so Razorpay can retry
            return ResponseEntity.internalServerError()
                    .body("Webhook processing failed");
        }
    }
}