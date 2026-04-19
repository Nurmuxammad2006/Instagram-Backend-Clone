package com.example.instagrambackend.repository;

import com.example.instagrambackend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String username, String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE id = :id", nativeQuery = true)
    void incrementFailedAttempts(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET failed_login_attempts = 0, lock_time = null WHERE id = :id", nativeQuery = true)
    void resetFailedAttempts(@Param("id") Long id);

    @Modifying
    @Transactional // Remove the (propagation = Propagation.REQUIRES_NEW)
    @Query(value = "UPDATE users SET lock_time = :lockTime, failed_login_attempts = 5 WHERE id = :id", nativeQuery = true)
    void lockUser(@Param("id") Long id, @Param("lockTime") ZonedDateTime lockTime);
}