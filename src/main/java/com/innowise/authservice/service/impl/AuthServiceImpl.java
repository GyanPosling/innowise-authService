package com.innowise.authservice.service.impl;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.AccessTokenRejectedException;
import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.exception.CredentialsConflictException;
import com.innowise.authservice.exception.LoginFailedException;
import com.innowise.authservice.exception.RefreshTokenRejectedException;
import com.innowise.authservice.exception.TokenValidationFailedException;
import com.innowise.authservice.mapper.AuthUserMapper;
import com.innowise.authservice.client.UserServiceClient;
import com.innowise.authservice.model.dto.request.InternalUserCreateRequest;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegisterRequest;
import com.innowise.authservice.model.dto.request.ValidateTokenRequest;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.TokenType;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.CustomUserDetailsService;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthUserRepository authUserRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final CustomUserDetailsService customUserDetailsService;
  private final AuthUserMapper authUserMapper;
  private final UserServiceClient userServiceClient;

  @Override
  public RegisterResponse register(RegisterRequest request) {
    Integer userId = userServiceClient.createInternalUser(
        InternalUserCreateRequest.builder()
            .name(request.getName())
            .surname(request.getSurname())
            .birthDate(request.getBirthDate())
            .email(request.getEmail())
            .build());
    if (userId == null) {
      throw new IllegalStateException("User service did not return user id");
    }

    if (authUserRepository.existsByUsername(request.getUsername())) {
      userServiceClient.rollbackUser(userId);
      throw new CredentialsConflictException("username", request.getUsername());
    }
    if (authUserRepository.existsByEmail(request.getEmail())) {
      userServiceClient.rollbackUser(userId);
      throw new CredentialsConflictException("email", request.getEmail());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    AuthUser user = authUserMapper.toEntity(request, encodedPassword);
    AuthUser savedUser = authUserRepository.save(user);
    try {
      userServiceClient.linkAuthUser(userId, savedUser.getId());
    } catch (RuntimeException ex) {
      authUserRepository.deleteById(savedUser.getId());
      userServiceClient.rollbackUser(userId);
      throw ex;
    }

    UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());
    TokenResponse tokens = jwtService.generateTokens(userDetails);
    return RegisterResponse.builder()
        .userId(savedUser.getId())
        .username(savedUser.getUsername())
        .email(savedUser.getEmail())
        .role(savedUser.getRole())
        .accessToken(tokens.getAccessToken())
        .refreshToken(tokens.getRefreshToken())
        .tokenType(tokens.getTokenType())
        .build();
  }

  @Override
  public TokenResponse createTokens(LoginRequest request) {
    if (authUserRepository.findByUsername(request.getUsername()).isEmpty()) {
      throw new AuthUserNotFoundException("username", request.getUsername());
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());
      return jwtService.generateTokens(userDetails);
    } catch (BadCredentialsException ex) {
      throw new LoginFailedException("Incorrect username or password");
    }
  }

  @Override
  public TokenResponse refreshTokens(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();
    try {
      jwtService.validateToken(refreshToken);
      if (jwtService.extractTokenType(refreshToken) != TokenType.REFRESH) {
        throw new RefreshTokenRejectedException();
      }
    } catch (JwtException | IllegalArgumentException ex) {
      throw new RefreshTokenRejectedException();
    }

    String username = jwtService.extractUsername(refreshToken);
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
    return jwtService.refreshTokens(refreshToken, userDetails);
  }

  @Override
  public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
    String token = request.getToken();
    try {
      jwtService.validateToken(token);
    } catch (JwtException | IllegalArgumentException ex) {
      throw new AccessTokenRejectedException();
    }

    try {
      String username = jwtService.extractUsername(token);
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      if (!(userDetails instanceof AuthUserDetails authUserDetails)) {
        throw new TokenValidationFailedException("Unsupported user details");
      }

      return authUserMapper.toValidateTokenResponse(authUserDetails);
    } catch (AuthUserNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new TokenValidationFailedException("Failed to validate token");
    }
  }
}
