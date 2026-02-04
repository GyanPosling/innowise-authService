package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.request.CreateCredentialsRequest;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.ValidateTokenRequest;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;

public interface AuthService {

  RegisterResponse createCredentials(CreateCredentialsRequest request);

  TokenResponse createTokens(LoginRequest request);

  TokenResponse refreshTokens(RefreshTokenRequest request);

  ValidateTokenResponse validateToken(ValidateTokenRequest request);
}
