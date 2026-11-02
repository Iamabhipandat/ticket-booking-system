package com.abhinav.ticketbooking.service.refund;

import com.abhinav.ticketbooking.entity.payment.Payment;
import com.abhinav.ticketbooking.entity.payment.PaymentStatus;
import com.abhinav.ticketbooking.entity.payment.RefundStatus;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

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
    public Payment processRefund(Long paymentRecordId) throws RazorpayException {
        // Find our local payment record
        Payment payment = paymentRepository.findById(paymentRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + paymentRecordId));

        // Don't create another refund if one already exists
        if (payment.getRazorpayRefundId() != null && !payment.getRazorpayRefundId().isBlank()) {
            return payment;
        }

        // Refunds are allowed only for captured payments awaiting refund
        if (payment.getStatus() != PaymentStatus.REFUND_PENDING) {
            throw new IllegalStateException("Payment is not awaiting a refund (Status: " + payment.getStatus() + ")");
        }

        String razorpayPaymentId = payment.getRazorpayPaymentId();
        if (razorpayPaymentId == null || razorpayPaymentId.isBlank()) {
            throw new BadRequestException("Razorpay payment ID is missing from payment record");
        }

        // Request a full refund from Razorpay
        Refund razorpayRefund = razorpayClient.payments.refund(razorpayPaymentId);

        // Save the refund ID returned by Razorpay
        String refundId = razorpayRefund.get("id");
        payment.setRazorpayRefundId(refundId);
        payment.setRefundStatus(RefundStatus.PENDING);

        log.info("Initiated refund {} with Razorpay for payment #{}", refundId, paymentRecordId);
        return paymentRepository.save(payment);
    }
}
