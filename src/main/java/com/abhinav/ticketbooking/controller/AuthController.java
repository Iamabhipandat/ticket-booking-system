package com.abhinav.ticketbooking.controller;

import com.abhinav.ticketbooking.dto.AuthResponse;
import com.abhinav.ticketbooking.dto.RegisterRequest;
import com.abhinav.ticketbooking.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Register a new user
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }
}