package com.example.instagrambackend.service;

import com.example.instagrambackend.domain.entity.EmailVerification;
import com.example.instagrambackend.domain.entity.PasswordResetToken;
import com.example.instagrambackend.domain.entity.RefreshToken;
import com.example.instagrambackend.domain.entity.User;
import com.example.instagrambackend.domain.enums.Role;
import com.example.instagrambackend.domain.enums.VerificationType;
import com.example.instagrambackend.domain.requests.Login;
import com.example.instagrambackend.domain.requests.Register;
import com.example.instagrambackend.domain.response.AuthResponse;
import com.example.instagrambackend.exception.AppException;
import com.example.instagrambackend.exception.UserAlreadyExistsException;
import com.example.instagrambackend.repository.AuthRepository;
import com.example.instagrambackend.repository.EmailVerificationRepository;
import com.example.instagrambackend.repository.PasswordResetTokenRepository;
import com.example.instagrambackend.repository.RefreshTokenRepository;
import jakarta.mail.MessagingException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    // Helper method to create refresh token
    private RefreshToken createRefreshToken(User user) {
        // Revoke old tokens first
        refreshTokenRepository.revokeAllByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        jwtService.getRefreshTokenExpiration() / 1000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // REGISTER
    @Transactional
    public AuthResponse registerUser(Register register) {

        if (authRepository.findByEmail(register.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (authRepository.findByUsername(register.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
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

        log.info("New user registered: {}", register.getEmail());

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName()
        );
    }

    // LOGIN
    @Transactional(noRollbackFor = AppException.class)
    public AuthResponse loginUser(Login login) {
        User user = authRepository.findByUsernameOrEmail(
                login.getUsernameOrEmail(),
                login.getUsernameOrEmail()
        ).orElseThrow(() -> {
            log.warn("Login failed - user not found: {}", login.getUsernameOrEmail());
            return new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        });

        // 1. Check if account is locked
        if (user.getLockTime() != null) {
            if (user.accountLocked()) {
                log.warn("Login attempted on locked account: {}", user.getEmail());
                throw new AppException(
                        "Account is locked. Try again after 15 minutes.",
                        HttpStatus.LOCKED
                );
            } else {
                // LOCK EXPIRED: Reset and proceed
                authRepository.resetFailedAttempts(user.getId());
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                log.info("Lock expired for user: {}. Counter reset.", user.getEmail());
            }
        }

        // 2. Password Check
        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            authRepository.incrementFailedAttempts(user.getId());

            // Check if this latest failure triggers a lock (we use +1 because user object is stale)
            if (user.getFailedLoginAttempts() + 1 >= 5) {
                authRepository.lockUser(user.getId(), ZonedDateTime.now());
                log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
                throw new AppException(
                        "Too many failed attempts. Account locked for 15 minutes.",
                        HttpStatus.LOCKED
                );
            }

            log.warn("Login failed - wrong password for: {}", user.getEmail());
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        // 3. Successful login - reset failed attempts
        authRepository.resetFailedAttempts(user.getId());
        log.info("User logged in successfully: {}", user.getEmail());

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName()
        );
    }

    // Refresh access token
    @Transactional
    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new AppException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException("Refresh token has expired, please login again");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new AuthResponse(
                newAccessToken,
                refreshToken.getToken(), // same refresh token
                user.getEmail(),
                user.getUsername(),
                user.getFullName()
        );
    }

    // Logout
    @Transactional
    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    // MERGED: handles both EMAIL_VERIFICATION and PASSWORD_RESET
    @Transactional
    public void sendCode(String email, VerificationType type) throws MessagingException {
        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email).orElse(null);
        if (user == null) return; // silent fail for security

        if (type == VerificationType.EMAIL_VERIFICATION && user.isVerified()) {
            throw new AppException("Email already verified", HttpStatus.CONFLICT);
        }

        String code = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        emailVerificationRepository.save(verification);

        log.info("Verification code sent to: {} type: {}", email, type);

        emailService.sendCodeToEmail(email, code);
    }

    // MERGED: handles both EMAIL_VERIFICATION and PASSWORD_RESET
    @Transactional
    public Object verifyCode(String email, String code, VerificationType type) {
        email = email.trim().toLowerCase();

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found"));

        EmailVerification verification = emailVerificationRepository
                .findTopByUserAndCodeAndTypeAndUsedFalseOrderByCreatedAtDesc(user, code, type)
                .orElseThrow(() -> new AppException("Invalid verification code"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException("Verification code has expired");
        }

        verification.setUsed(true);
        emailVerificationRepository.save(verification);

        if (type == VerificationType.EMAIL_VERIFICATION) {
            user.setVerified(true);
            authRepository.save(user);
            return null;
        }

        // PASSWORD_RESET → generate secure token
        passwordResetTokenRepository.invalidateAllByUserId(user.getId());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        passwordResetTokenRepository.save(resetToken);

        return resetToken.getToken();
    }

    // resetPassword
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {

        PasswordResetToken tokenEntity = passwordResetTokenRepository.findByToken(resetToken)
                .orElseThrow(() -> new AppException("Invalid reset token"));

        if (tokenEntity.isUsed()) {
            throw new AppException("Reset token has already been used");
        }

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException("Reset token has expired");
        }

        User user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(user);

        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(Long userId) {
        authRepository.incrementFailedAttempts(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(Long userId) {
        authRepository.resetFailedAttempts(userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockUser(Long userId, ZonedDateTime lockTime) {
        authRepository.lockUser(userId, lockTime);
    }
}