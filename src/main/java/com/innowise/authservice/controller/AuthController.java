package com.innowise.authservice.controller;

import com.innowise.authservice.controller.api.AuthControllerApi;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegisterRequest;
import com.innowise.authservice.model.dto.request.ValidateTokenRequest;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;
import com.innowise.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerApi {

    private final AuthService authService;

    @PostMapping("/register")
    @Override
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.createTokens(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Override
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshTokens(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Override
    public ResponseEntity<ValidateTokenResponse> validateToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        ValidateTokenResponse response = authService.validateToken(request);
        return ResponseEntity.ok(response);
    }
}

