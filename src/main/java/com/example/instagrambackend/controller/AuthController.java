package com.example.instagrambackend.controller;

import com.example.instagrambackend.domain.requests.Register;
import com.example.instagrambackend.domain.response.AuthResponse;
import com.example.instagrambackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid Register register) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(register));
    }

}
