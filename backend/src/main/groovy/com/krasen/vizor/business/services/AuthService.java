package com.krasen.vizor.business.services;

import com.krasen.vizor.business.IRepo.IUserRepository;
import com.krasen.vizor.business.exception.UserExceptions;
import com.krasen.vizor.business.domain.User;
import com.krasen.vizor.security.JwtService;
import com.krasen.vizor.web.DTOs.AuthDTOs.ForgotPasswordRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.JwtResponse;
import com.krasen.vizor.web.DTOs.AuthDTOs.LoginRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.RegisterRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.ResetPasswordRequest;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final IUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest req) {
        String email = normalizeEmail(req.email());
        String username = normalizeUsername(req.username());
        String password = normalizePassword(req.password());

        if (!req.isCreator() && !req.isOwner()) {
            throw UserExceptions.invalidRole();
        }

        if (userRepo.existsByEmail(email)) {
            throw UserExceptions.emailAlreadyExists(email);
        }

        if (userRepo.existsByUsername(username)) {
            throw UserExceptions.usernameAlreadyExists(username);
        }

        Set<String> roles = new HashSet<>();
        if (req.isCreator()) {
            roles.add("CREATOR");
        }
        if (req.isOwner()) {
            roles.add("OWNER");
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .roles(roles)
                .build();

        userRepo.save(user);
    }

    public JwtResponse login(LoginRequest req) {
        String emailOrUsername = normalizeEmailOrUsername(req.emailOrUsername());
        String password = normalizePassword(req.password());

        User user = userRepo.findByEmailOrUsername(emailOrUsername)
                .orElseThrow(() -> UserExceptions.invalidCredentials());

        if (user.getDeletedAt() != null) {
            throw UserExceptions.accountDeleted();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw UserExceptions.invalidCredentials();
        }

        // Convert Set to List for JWT serialization (JWT expects List/Array)
        List<String> rolesList = new ArrayList<>(user.getRoles());
        Map<String, Object> claims = Map.of(
                "userId", user.getId(),
                "roles", rolesList
        );
        String token = jwtService.generate(claims, user.getEmail());

        return new JwtResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRoles()
        );
    }

    public void requestPasswordReset(ForgotPasswordRequest req) {
        String email = normalizeEmail(req.email());
        User user = userRepo.findByEmailOrUsername(email)
                .orElseThrow(() -> UserExceptions.invalidCredentials());

        if (user.getDeletedAt() != null) {
            throw UserExceptions.accountDeleted();
        }

        Map<String, Object> claims = Map.of("type", "password-reset", "userId", user.getId());
        String token = jwtService.generate(claims, user.getEmail());

        // TODO: Send email with reset token
        // For now, token is generated but not sent anywhere
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        try {
            var jws = jwtService.parse(req.token());
            Claims claims = jws.getPayload();

            if (!"password-reset".equals(claims.get("type"))) {
                throw new IllegalArgumentException("Invalid token type");
            }

            Long userId = claims.get("userId", Long.class);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> UserExceptions.notFound(userId));

            if (user.getDeletedAt() != null) {
                throw UserExceptions.cannotUpdateDeleted();
            }

            String normalizedPassword = normalizePassword(req.newPassword());
            user.setPasswordHash(passwordEncoder.encode(normalizedPassword));
            user.setUpdatedAt(java.time.OffsetDateTime.now());
            userRepo.save(user);
        } catch (Exception e) {
            throw UserExceptions.invalidCredentials();
        }
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw UserExceptions.invalidEmail();
        }
        return email.trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw UserExceptions.invalidUsername();
        }
        return username.trim();
    }

    private String normalizeEmailOrUsername(String emailOrUsername) {
        if (!StringUtils.hasText(emailOrUsername)) {
            throw UserExceptions.invalidCredentials();
        }
        if (emailOrUsername.contains("@")) {
            return emailOrUsername.trim().toLowerCase();
        }
        return emailOrUsername.trim();
    }

    private String normalizePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw UserExceptions.invalidPassword();
        }
        return password.trim();
    }
}

