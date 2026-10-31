package com.abhinav.ticketbooking.service.refund;

import com.abhinav.ticketbooking.entity.payment.Payment;
import com.abhinav.ticketbooking.entity.payment.PaymentStatus;
import com.abhinav.ticketbooking.entity.payment.RefundStatus;
import com.abhinav.ticketbooking.repository.PaymentRepository;

import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.RazorpayException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    public RefundService(
            PaymentRepository paymentRepository,
            RazorpayClient razorpayClient
    ) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
    }

    @Transactional
    public Payment processRefund(Long paymentRecordId)
            throws RazorpayException {

        // Find our local payment record.
        Payment payment = paymentRepository.findById(paymentRecordId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment record not found"
                        )
                );

        // Don't create another refund if one already exists.
        if (payment.getRazorpayRefundId() != null
                && !payment.getRazorpayRefundId().isBlank()) {
            return payment;
        }

        // Refunds are allowed only for captured payments awaiting refund.
        if (payment.getStatus() != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException(
                    "Payment is not awaiting a refund"
            );
        }

        // A captured Razorpay payment ID is required.
        String razorpayPaymentId = payment.getRazorpayPaymentId();

        if (razorpayPaymentId == null
                || razorpayPaymentId.isBlank()) {
            throw new IllegalStateException(
                    "Razorpay payment ID is missing"
            );
        }

        // Request a full refund from Razorpay.
        Refund razorpayRefund =
                razorpayClient.payments.refund(razorpayPaymentId);

        // Save the refund ID returned by Razorpay.
        String refundId = razorpayRefund.get("id");

        payment.setRazorpayRefundId(refundId);
        payment.setRefundStatus(RefundStatus.PENDING);

        return paymentRepository.save(payment);
    }
}