package com.abhinav.ticketbooking.service.payment;

import com.abhinav.ticketbooking.dto.CreatePaymentOrderRequest;
import com.abhinav.ticketbooking.dto.PaymentOrderResponse;
import com.abhinav.ticketbooking.dto.VerifyPaymentRequest;
import com.abhinav.ticketbooking.entity.booking.Booking;
import com.abhinav.ticketbooking.entity.booking.BookingStatus;
import com.abhinav.ticketbooking.entity.payment.Payment;
import com.abhinav.ticketbooking.entity.payment.PaymentStatus;
import com.abhinav.ticketbooking.repository.BookingRepository;
import com.abhinav.ticketbooking.repository.PaymentRepository;
import com.abhinav.ticketbooking.service.Booking.BookingService;
import com.abhinav.ticketbooking.service.Booking.TicketService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(
            RazorpayClient razorpayClient,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            BookingService bookingService,
            TicketService ticketService
    ) {
        this.razorpayClient = razorpayClient;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.ticketService = ticketService;
    }

    // Create Razorpay order
    @Transactional
    public PaymentOrderResponse createPaymentOrder(
            CreatePaymentOrderRequest request
    ) throws RazorpayException {

        if (request == null || request.getBookingId() == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking is not pending payment");
        }

        if (booking.getHoldExpiresAt() == null ||
                booking.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Booking hold has expired");
        }

        if (paymentRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException(
                    "Payment order already exists for this booking");
        }

        BigDecimal amountInRupees = booking.getTotalAmount();

        if (amountInRupees == null ||
                amountInRupees.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Invalid booking amount");
        }

        // Razorpay expects amount in paise
        int amountInPaise = amountInRupees
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "booking_" + booking.getId());

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setAmount(amountInRupees);
        payment.setStatus(PaymentStatus.CREATED);

        paymentRepository.save(payment);

        return new PaymentOrderResponse(
                razorpayOrder.get("id"),
                razorpayKeyId,
                amountInPaise,
                "INR",
                booking.getId()
        );
    }

    // Verify Razorpay payment and issue ticket
    @Transactional
    public String verifyPayment(VerifyPaymentRequest request)
            throws RazorpayException {

        if (request == null ||
                request.getRazorpayOrderId() == null ||
                request.getRazorpayPaymentId() == null ||
                request.getRazorpaySignature() == null) {

            throw new IllegalArgumentException(
                    "Payment verification details are required");
        }

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment not found"));

        // Idempotency: do not confirm or issue a duplicate ticket
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return "Payment already verified";
        }

        JSONObject verificationData = new JSONObject();
        verificationData.put(
                "razorpay_order_id",
                request.getRazorpayOrderId()
        );
        verificationData.put(
                "razorpay_payment_id",
                request.getRazorpayPaymentId()
        );
        verificationData.put(
                "razorpay_signature",
                request.getRazorpaySignature()
        );

        boolean signatureValid = Utils.verifyPaymentSignature(
                verificationData,
                razorpayKeySecret
        );

        if (!signatureValid) {
            throw new IllegalArgumentException(
                    "Invalid payment signature");
        }

        payment.setRazorpayPaymentId(
                request.getRazorpayPaymentId());

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        Booking booking = payment.getBooking();

        // Confirm the booking and mark seats as BOOKED
        Booking confirmedBooking =
                bookingService.confirmBooking(booking.getId());

        // Issue ticket only after booking confirmation
        ticketService.issueTicket(confirmedBooking.getId());

        return "Payment verified, booking confirmed, and ticket issued";
    }
}