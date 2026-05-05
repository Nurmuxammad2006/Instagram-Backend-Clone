package com.example.chatwavebackend.repository;

import com.example.chatwavebackend.domain.entity.RefreshToken;
import com.example.chatwavebackend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Revoke all tokens for a user (used on logout)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllByUser(User user);
}