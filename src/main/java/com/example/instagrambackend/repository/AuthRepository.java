package com.example.instagrambackend.repository;

import com.example.instagrambackend.model.AuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<AuthModel, Long>{
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<AuthModel> findByEmail(String email);
    Optional<AuthModel> findById(Long id);
}
