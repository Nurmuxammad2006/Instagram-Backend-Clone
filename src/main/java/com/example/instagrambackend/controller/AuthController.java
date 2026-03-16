package com.example.instagrambackend.controller;

import com.example.instagrambackend.dto.AuthDTO;
import com.example.instagrambackend.model.AuthModel;
import com.example.instagrambackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public AuthModel save (@RequestBody AuthModel authModel){
        System.out.println("User Successfully created");
        return authService.save(authModel);
    }

    @PostMapping("login")
    public ResponseEntity<?> login (@RequestBody AuthDTO authDTO){
        AuthModel userLogin = authService.login(authDTO.getEmail(), authDTO.getPassword());
        if (userLogin == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or Password");
        }
        return ResponseEntity.ok(userLogin);
    }

}
