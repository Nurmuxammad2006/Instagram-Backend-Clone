package com.example.instagrambackend.service;

import com.example.instagrambackend.domain.entity.User;
import com.example.instagrambackend.domain.enums.Role;
import com.example.instagrambackend.domain.requests.Register;
import com.example.instagrambackend.domain.response.AuthResponse;
import com.example.instagrambackend.domain.response.BaseResponse;
import com.example.instagrambackend.exception.UserAlreadyExistsException;
import com.example.instagrambackend.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public AuthResponse registerUser( Register register) {

        Optional<User> existingUserByEmail = authRepository.findByEmail(register.getEmail());

        if (existingUserByEmail.isPresent()) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        if (authRepository.findByUsername(register.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(register.getPassword());
        register.setPassword(hashedPassword);

        User user = User.builder()
                .fullName(register.getFullName())
                .username(register.getUsername())
                .email(register.getEmail())
                .password(register.getPassword())
                .role(Role.USER)
                .build();

        authRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getEmail(), user.getUsername(), user.getFullName());
    }
}
