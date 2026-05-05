package com.example.chatwavebackend.controller;

import com.example.chatwavebackend.domain.enums.VerificationType;
import com.example.chatwavebackend.domain.requests.Login;
import com.example.chatwavebackend.domain.requests.Register;
import com.example.chatwavebackend.domain.response.AuthResponse;
import com.example.chatwavebackend.domain.response.BaseResponse;
import com.example.chatwavebackend.domain.response.PasswordResetResponse;
import com.example.chatwavebackend.exception.AppException;
import com.example.chatwavebackend.service.AuthService;
import com.example.chatwavebackend.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;   // Add this import

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    // Helper method to extract IP — add this to the controller
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid Register register) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(register));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid Login login,
            HttpServletRequest request) {

        if (!rateLimitService.tryLoginConsume(getClientIp(request))) {
            throw new AppException("Too many login attempts. Please wait 1 minute.", HttpStatus.TOO_MANY_REQUESTS);
        }

        return ResponseEntity.ok(authService.loginUser(login));
    }

    // ? MERGED: /send-code?type=EMAIL_VERIFICATION or PASSWORD_RESET
    @PostMapping("/send-code")
    public BaseResponse sendCode(
            @RequestBody Map<String, String> request,
            @RequestParam(defaultValue = "EMAIL_VERIFICATION") VerificationType type,
            HttpServletRequest httpRequest) {

        if (!rateLimitService.trySendCodeConsume(getClientIp(httpRequest))) {
            throw new AppException("Too many requests. Please wait 10 minutes.", HttpStatus.TOO_MANY_REQUESTS);
        }

        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        try {
            authService.sendCode(email.trim().toLowerCase(), type);
            BaseResponse response = new BaseResponse();
            response.setMessage("Code sent successfully");
            response.setCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            throw new AppException("Failed to send code: " + e.getMessage());
        }
    }

    // ? MERGED: /verify-code?type=EMAIL_VERIFICATION or PASSWORD_RESET
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(
            @RequestBody Map<String, String> request,
            @RequestParam(defaultValue = "EMAIL_VERIFICATION") VerificationType type) {

        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            throw new IllegalArgumentException("Email and code are required");
        }

        Object result = authService.verifyCode(email.trim().toLowerCase(), code.trim(), type);

        if (type == VerificationType.PASSWORD_RESET) {
            return ResponseEntity.ok(new PasswordResetResponse(
                    (String) result,
                    "Code verified. Use the resetToken to reset your password."
            ));
        }

        BaseResponse response = new BaseResponse();
        response.setMessage("Email verified successfully");
        response.setCode(HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public BaseResponse resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");

        if (resetToken == null || newPassword == null) {
            throw new IllegalArgumentException("Reset token and new password are required");
        }

        authService.resetPassword(resetToken.trim(), newPassword.trim());

        BaseResponse response = new BaseResponse();
        response.setMessage("Password reset successfully");
        response.setCode(HttpStatus.OK.value());
        return response;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String token = request.get("refreshToken");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    public BaseResponse logout(@RequestBody Map<String, String> request) {
        String token = request.get("refreshToken");
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        authService.logout(token);
        BaseResponse response = new BaseResponse();
        response.setMessage("Logged out successfully");
        response.setCode(HttpStatus.OK.value());
        return response;
    }
}