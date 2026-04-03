package com.example.instagrambackend.service;

import com.example.instagrambackend.domain.entity.EmailVerification;
import com.example.instagrambackend.domain.entity.User;
import com.example.instagrambackend.domain.enums.Role;
import com.example.instagrambackend.domain.enums.VerificationType;
import com.example.instagrambackend.domain.requests.Login;
import com.example.instagrambackend.domain.requests.Register;
import com.example.instagrambackend.domain.response.AuthResponse;
import com.example.instagrambackend.exception.UserAlreadyExistsException;
import com.example.instagrambackend.repository.AuthRepository;
import com.example.instagrambackend.repository.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ✅ REGISTER
    public AuthResponse registerUser(Register register) {

        if (authRepository.findByEmail(register.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (authRepository.findByUsername(register.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .fullName(register.getFullName())
                .username(register.getUsername())
                .email(register.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(register.getPassword()))
                .role(Role.USER)
                .isVerified(false)
                .isEnabled(true)
                .build();

        authRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), user.getFullName());
    }

    // ✅ LOGIN
    public AuthResponse loginUser(Login login) {

        User user = authRepository.findByUsernameOrEmail(
                login.getUsernameOrEmail(),
                login.getUsernameOrEmail()
        ).orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), user.getFullName());
    }

    // ✅ SEND CODE Email verification code
    @Transactional
    public void sendCode(String email) throws MessagingException {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return;
        }

        if (user.isVerified()) {
            throw new RuntimeException("Email already verified");
        }

        String code = String.format("%06d",
                new SecureRandom().nextInt(900000) + 100000);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .code(code)
                .type(VerificationType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        emailVerificationRepository.save(verification);

        emailService.sendCodeToEmail(email, code);
    }

    // ✅ VERIFY CODE (MAIN FIX)
    @Transactional
    public void verifyCode(String email, String code) {

        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailVerification verification = emailVerificationRepository.findTopByUserAndCodeAndTypeAndUsedFalseOrderByCreatedAtDesc(user, code, VerificationType.EMAIL_VERIFICATION)
                        .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }

        verification.setUsed(true);
        emailVerificationRepository.save(verification);

        user.setVerified(true);
        authRepository.save(user);
    }

    // send forgot password code
    @Transactional
    public void sendResetCode(String email) throws MessagingException {

        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code = String.format("%06d",
                new SecureRandom().nextInt(900000) + 100000);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .code(code)
                .type(VerificationType.PASSWORD_RESET) // 🔥 important
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        emailVerificationRepository.save(verification);

        emailService.sendCodeToEmail(email, code);
    }

    // Verify forgot password code
    @Transactional
    public void verifyResetCode(String email, String code) {

        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailVerification verification = emailVerificationRepository.findTopByUserAndCodeAndTypeAndUsedFalseOrderByCreatedAtDesc(user, code, VerificationType.PASSWORD_RESET)
                        .orElseThrow(() -> new RuntimeException("Invalid code"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expired");
        }

        verification.setUsed(true);
        emailVerificationRepository.save(verification);

        user.setCanResetPassword(true);
        authRepository.save(user);
    }

    // Reset password forgot password
    @Transactional
    public void resetPassword(String email, String newPassword) {

        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isCanResetPassword()) {
            throw new RuntimeException("Reset not allowed");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        user.setCanResetPassword(false);

        authRepository.save(user);
    }
}