package com.example.instagrambackend.service;

import com.example.instagrambackend.dto.UpdateProfileDTO;
import com.example.instagrambackend.model.AuthModel;
import com.example.instagrambackend.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthModel save(AuthModel authModel) throws DataIntegrityViolationException {

        if (authRepository.existsByUsername(authModel.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        if (authRepository.existsByUsername(authModel.getUsername())){
            throw new RuntimeException(("Username exists"));
        }

        if (authRepository.existsByPhoneNumber(authModel.getPhoneNumber())){
            throw new RuntimeException(("Phone already in use"));
        }

        authModel.setPassword(passwordEncoder.encode(authModel.getPassword()));
        return authRepository.save(authModel);
    }

    public AuthModel login (String email, String password){
        Optional<AuthModel> user = authRepository.findByEmail(email);

        if (user.isPresent()){
            if (passwordEncoder.matches(password, user.get().getPassword())){
                return user.get();
            }
        }
        return null;
    }

    public List<AuthModel> findAll(){
        return authRepository.findAll();
    }

    public ResponseEntity<?> findById(Long id) {
        Optional<AuthModel> findId = authRepository.findById(id);
        if (findId.isPresent()) {
            return ResponseEntity.ok(findId.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    public AuthModel updateUser(Long id, UpdateProfileDTO profileDTO) {
        AuthModel existingUser = authRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (profileDTO.getName() != null) existingUser.setName(profileDTO.getName());
        if (profileDTO.getSurname() != null) existingUser.setSurname(profileDTO.getSurname());
        if (profileDTO.getUsername() != null) existingUser.setUsername(profileDTO.getUsername());
        if (profileDTO.getBio() != null) existingUser.setBio(profileDTO.getBio());
        if (profileDTO.getGender() != null) existingUser.setGender(profileDTO.getGender());
        if (profileDTO.getBirthDate() != null) existingUser.setBirthday(profileDTO.getBirthDate());

        return authRepository.save(existingUser);
    }
}
