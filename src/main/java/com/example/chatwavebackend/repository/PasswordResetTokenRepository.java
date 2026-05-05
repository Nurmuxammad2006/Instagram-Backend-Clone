package com.example.chatwavebackend.repository;

import com.example.chatwavebackend.domain.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // Invalidate all previous tokens for user
    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user.id = :userId")
    void invalidateAllByUserId(Long userId);
}