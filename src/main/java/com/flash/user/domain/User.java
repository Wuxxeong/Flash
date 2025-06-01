package com.flash.user.domain;

import com.flash.common.exception.ValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Builder
    public User(String email, String password, String name) {
        validateEmail(email);
        validatePassword(password);
        validateName(name);
        
        this.email = email;
        this.password = password;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException.InvalidEmailException();
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new ValidationException.InvalidEmailException();
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException.InvalidPasswordException();
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException.InvalidPasswordException();
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException.EmptyNameException();
        }
    }
} 