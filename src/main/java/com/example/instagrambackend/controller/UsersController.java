package com.example.instagrambackend.controller;

import com.example.instagrambackend.dto.UpdateProfileDTO;
import com.example.instagrambackend.model.AuthModel;
import com.example.instagrambackend.repository.AuthRepository;
import com.example.instagrambackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id){
        return authService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser (@PathVariable Long id, @RequestBody @Validated UpdateProfileDTO updateProfileDTO){
        AuthModel update = authService.updateUser(id, updateProfileDTO);
        return ResponseEntity.ok(update);
    }
}
