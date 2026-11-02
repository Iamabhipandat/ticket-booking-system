package com.abhinav.ticketbooking.service;

import com.abhinav.ticketbooking.dto.AuthResponse;
import com.abhinav.ticketbooking.dto.LoginRequest;
import com.abhinav.ticketbooking.dto.LoginResponse;
import com.abhinav.ticketbooking.dto.RegisterRequest;
import com.abhinav.ticketbooking.entity.auth.Role;
import com.abhinav.ticketbooking.entity.auth.User;
import com.abhinav.ticketbooking.entity.auth.VerificationToken;
import com.abhinav.ticketbooking.exception.BadRequestException;
import com.abhinav.ticketbooking.exception.ResourceNotFoundException;
import com.abhinav.ticketbooking.repository.UserRepository;
import com.abhinav.ticketbooking.repository.VerificationTokenRepository;
import com.abhinav.ticketbooking.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            VerificationTokenRepository verificationTokenRepository,
            EmailService emailService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Name is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_CUSTOMER);
        user.setVerified(false);

        User savedUser = userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken(savedUser);
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                verificationToken.getToken()
        );

        return new AuthResponse(
                "Registration successful. Please check your email to verify your account.",
                savedUser.getEmail()
        );
    }

    @Transactional
    public AuthResponse verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Verification token is required");
        }

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new BadRequestException("Verification token has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return new AuthResponse("Email verified successfully! You can now log in.", user.getEmail());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isVerified()) {
            throw new IllegalStateException("Your email is not verified. Please verify your account before logging in.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(
                token,
                "Bearer",
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getId()
        );
    }

    @Transactional
    public AuthResponse resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        if (user.isVerified()) {
            return new AuthResponse("Account is already verified. Please log in.", user.getEmail());
        }

        verificationTokenRepository.findByUserId(user.getId())
                .ifPresent(verificationTokenRepository::delete);

        VerificationToken newToken = new VerificationToken(user);
        verificationTokenRepository.save(newToken);

        emailService.sendVerificationEmail(user.getEmail(), newToken.getToken());

        return new AuthResponse("A new verification link has been sent to your email.", user.getEmail());
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
