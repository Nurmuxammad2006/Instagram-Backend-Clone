package com.example.instagrambackend.repository;

import com.example.instagrambackend.model.AuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<AuthModel, Long>{
    boolean existsByEmail(String email);
    Optional<AuthModel> findByEmail(String email);
}
