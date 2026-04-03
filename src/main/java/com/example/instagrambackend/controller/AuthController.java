package com.example.instagrambackend.controller;

import com.example.instagrambackend.domain.requests.Login;
import com.example.instagrambackend.domain.requests.Register;
import com.example.instagrambackend.domain.response.AuthResponse;
import com.example.instagrambackend.domain.response.BaseResponse;
import com.example.instagrambackend.service.AuthService;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid Register register) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(register));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid Login login) {
        return ResponseEntity.ok(authService.loginUser(login));
    }

    // send-code
    @PostMapping("/send-code")
    public BaseResponse sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        try {
            authService.sendCode(email.trim().toLowerCase());
            BaseResponse response = new BaseResponse();
            response.setMessage("Verification code sent successfully");
            response.setCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send code: " + e.getMessage());
        }
    }

    // verify-code
    @PostMapping("/verify-code")
    public BaseResponse verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            throw new IllegalArgumentException("Email and code are required");
        }

        authService.verifyCode(email.trim().toLowerCase(), code.trim());

        BaseResponse response = new BaseResponse();
        response.setMessage("Verification successful");
        response.setCode(HttpStatus.OK.value());
        return response;
    }

    @PostMapping("/forgot-password")
    public BaseResponse forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        try {
            authService.sendResetCode(email.trim().toLowerCase());
            BaseResponse response = new BaseResponse();
            response.setMessage("Password reset code sent successfully");
            response.setCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset code: " + e.getMessage());
        }
    }

    @PostMapping("/verify-reset-code")
    public BaseResponse verifyResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            throw new IllegalArgumentException("Email and code are required");
        }

        authService.verifyResetCode(email.trim().toLowerCase(), code.trim());

        BaseResponse response = new BaseResponse();
        response.setMessage("Password reset code verified successfully");
        response.setCode(HttpStatus.OK.value());
        return response;
    }

    @PostMapping("/reset-password")
    public BaseResponse resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || newPassword == null) {
            throw new IllegalArgumentException("Email and new password are required");
        }

    authService.resetPassword(email.trim().toLowerCase(), newPassword.trim());

        BaseResponse response = new BaseResponse();
        response.setMessage("Password reset successfully");
        response.setCode(HttpStatus.OK.value());
        return response;
    }
}