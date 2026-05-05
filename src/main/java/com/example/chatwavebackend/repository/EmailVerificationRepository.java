package com.example.chatwavebackend.repository;

import com.example.chatwavebackend.domain.entity.EmailVerification;
import com.example.chatwavebackend.domain.entity.User;
import com.example.chatwavebackend.domain.enums.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification>findTopByUserAndCodeAndTypeAndUsedFalseOrderByCreatedAtDesc(
            User user,
            String code,
            VerificationType type
    );

}