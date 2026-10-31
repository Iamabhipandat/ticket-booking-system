package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.CreatePaymentOrderRequest;
import com.abhinav.ticketbooking.dto.PaymentOrderResponse;
import com.abhinav.ticketbooking.dto.VerifyPaymentRequest;
import com.abhinav.ticketbooking.service.payment.PaymentService;

import com.razorpay.RazorpayException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }


    // =====================================================
    // CREATE RAZORPAY ORDER
    // =====================================================

    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            @RequestBody CreatePaymentOrderRequest request
    ) throws RazorpayException {

        PaymentOrderResponse response =
                paymentService.createPaymentOrder(request);

        return ResponseEntity.ok(response);
    }


    // =====================================================
    // VERIFY PAYMENT
    // =====================================================

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(
            @RequestBody VerifyPaymentRequest request
    ) throws RazorpayException {

        String result =
                paymentService.verifyPayment(request);

        return ResponseEntity.ok(result);
    }
}