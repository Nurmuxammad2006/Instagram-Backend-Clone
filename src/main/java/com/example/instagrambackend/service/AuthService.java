package com.example.instagrambackend.service;

import com.example.instagrambackend.model.AuthModel;
import com.example.instagrambackend.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthModel save(AuthModel authModel) {
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

}
