package com.example.chatwavebackend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.chatwavebackend.domain.enums.Role;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(unique = true, nullable = false)
    @Email(message = "Enter valid email")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @Column(length = 150)
    private String bio;

    @Column(length = 500)
    private String profilePicture;

    @Column(name = "is_private")
    private boolean isPrivate = false;

    @Column(name = "is_verified")
    private boolean isVerified = false;


    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role = Role.USER;

    @Column(name = "is_enabled")
    private boolean isEnabled = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailVerification> emailTokens;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "lock_time")
    private ZonedDateTime lockTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public boolean accountLocked() {
        if (lockTime == null) return false;
        return lockTime.plusMinutes(15).isAfter(ZonedDateTime.now());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}