package com.krasen.vizor.web.controller;

import com.krasen.vizor.business.services.AuthService;
import com.krasen.vizor.web.DTOs.AuthDTOs.ForgotPasswordRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.JwtResponse;
import com.krasen.vizor.web.DTOs.AuthDTOs.LoginRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.RegisterRequest;
import com.krasen.vizor.web.DTOs.AuthDTOs.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(value = "/auth", produces = "application/json")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping(value = "/register", consumes = "application/json")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest req,
                                         UriComponentsBuilder uri) {
        auth.register(req);
        return ResponseEntity.created(uri.path("/auth/login").build().toUri()).build();
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @PostMapping(value = "/forgot-password", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        auth.requestPasswordReset(req);
    }

    @PostMapping(value = "/reset-password", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        auth.resetPassword(req);
    }
}

