package com.abhinav.ticketbooking.repository;

import com.abhinav.ticketbooking.entity.auth.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}