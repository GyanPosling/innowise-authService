package com.innowise.authService.service;

import com.innowise.authService.model.dto.request.CreateCredentialsRequest;
import com.innowise.authService.model.dto.request.LoginRequest;
import com.innowise.authService.model.dto.request.RefreshTokenRequest;
import com.innowise.authService.model.dto.request.ValidateTokenRequest;
import com.innowise.authService.model.dto.response.RegisterResponse;
import com.innowise.authService.model.dto.response.TokenResponse;
import com.innowise.authService.model.dto.response.ValidateTokenResponse;

public interface AuthService {

  RegisterResponse createCredentials(CreateCredentialsRequest request);

  TokenResponse createTokens(LoginRequest request);

  TokenResponse refreshTokens(RefreshTokenRequest request);

  ValidateTokenResponse validateToken(ValidateTokenRequest request);
}
