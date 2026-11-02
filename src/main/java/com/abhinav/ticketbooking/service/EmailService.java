package com.abhinav.ticketbooking.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@ticketbooking.com}")
    private String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send email verification link to user after registration.
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        String subject = "Verify your Ticket Booking Account";
        String content = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #333;">Welcome to Ticket Booking!</h2>
                    <p>Thank you for registering. Please click the button below to verify your email address:</p>
                    <div style="margin: 25px 0;">
                        <a href="%s" style="background-color: #e50914; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;">
                            Verify Email Address
                        </a>
                    </div>
                    <p>Or copy and paste this link in your browser:</p>
                    <p><a href="%s">%s</a></p>
                    <p style="color: #777; font-size: 12px;">This link will expire in 30 minutes. If you did not register, please ignore this email.</p>
                </div>
                """.formatted(verificationUrl, verificationUrl, verificationUrl);

        sendHtmlEmail(toEmail, subject, content, null, null);
    }

    /**
     * Send ticket booking confirmation with QR code attachment.
     */
    public void sendTicketConfirmationEmail(
            String toEmail,
            String ticketCode,
            byte[] qrCodeBytes,
            String movieTitle,
            String showTime,
            List<String> seats,
            BigDecimal amount
    ) {
        String subject = "Booking Confirmed! Your Ticket for " + movieTitle;
        String seatList = String.join(", ", seats);
        String content = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #2e7d32;">🎉 Booking Confirmed!</h2>
                    <p>Your tickets have been successfully booked.</p>
                    <table style="width: 100%%; margin: 20px 0; border-collapse: collapse;">
                        <tr><td style="padding: 8px 0; color: #666;">Movie:</td><td style="font-weight: bold;">%s</td></tr>
                        <tr><td style="padding: 8px 0; color: #666;">Show Time:</td><td style="font-weight: bold;">%s</td></tr>
                        <tr><td style="padding: 8px 0; color: #666;">Seats:</td><td style="font-weight: bold;">%s</td></tr>
                        <tr><td style="padding: 8px 0; color: #666;">Total Paid:</td><td style="font-weight: bold;">₹%s</td></tr>
                        <tr><td style="padding: 8px 0; color: #666;">Ticket Code:</td><td style="font-family: monospace; font-size: 14px; font-weight: bold;">%s</td></tr>
                    </table>
                    <p>Show the attached QR code at the cinema entry gate.</p>
                    <div style="text-align: center; margin: 20px 0;">
                        <img src="cid:qrCode" alt="Ticket QR Code" style="width: 200px; height: 200px; border: 1px solid #ddd;" />
                    </div>
                </div>
                """.formatted(movieTitle, showTime, seatList, amount.toPlainString(), ticketCode);

        sendHtmlEmail(toEmail, subject, content, "ticket-qr.png", qrCodeBytes);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent, String attachmentName, byte[] attachmentBytes) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email to {} with subject '{}' was skipped.", toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            boolean isMultipart = attachmentBytes != null && attachmentBytes.length > 0;
            MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (isMultipart) {
                ByteArrayResource resource = new ByteArrayResource(attachmentBytes);
                helper.addInline("qrCode", resource, "image/png");
                helper.addAttachment(attachmentName, resource);
            }

            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // Do not throw runtime exception to avoid breaking the user workflow if SMTP server is down
        }
    }
}
