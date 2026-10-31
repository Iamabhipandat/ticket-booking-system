package com.abhinav.ticketbooking.service;

import com.abhinav.ticketbooking.dto.AuthResponse;
import com.abhinav.ticketbooking.dto.RegisterRequest;
import com.abhinav.ticketbooking.entity.auth.User;
import com.abhinav.ticketbooking.entity.auth.VerificationToken;
import com.abhinav.ticketbooking.repository.UserRepository;
import com.abhinav.ticketbooking.repository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            VerificationTokenRepository verificationTokenRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Validate request
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Normalize email
        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // Check whether email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        // Create user
        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);

        // Never store a plain-text password
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // User must verify email before login
        user.setVerified(false);

        // Save user first
        User savedUser = userRepository.save(user);

        // Create verification token
        VerificationToken verificationToken =
                new VerificationToken(savedUser);

        // Save token in database
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                verificationToken.getToken()
        );

        // Return response
        return new AuthResponse(
                "Registration successful. Please check your email to verify your account.",
                savedUser.getEmail()
        );
    }
}