package com.example.instagrambackend.repository;

import com.example.instagrambackend.domain.entity.EmailVerification;
import com.example.instagrambackend.domain.entity.User;
import com.example.instagrambackend.domain.enums.VerificationType;
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