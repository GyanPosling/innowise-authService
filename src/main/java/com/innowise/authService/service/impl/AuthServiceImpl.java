package com.innowise.authService.service.impl;

import com.innowise.authService.config.security.AuthUserDetails;
import com.innowise.authService.exception.AccessTokenRejectedException;
import com.innowise.authService.exception.AuthUserNotFoundException;
import com.innowise.authService.exception.CredentialsConflictException;
import com.innowise.authService.exception.LoginFailedException;
import com.innowise.authService.exception.RefreshTokenRejectedException;
import com.innowise.authService.exception.TokenValidationFailedException;
import com.innowise.authService.model.dto.request.CreateCredentialsRequest;
import com.innowise.authService.model.dto.request.LoginRequest;
import com.innowise.authService.model.dto.request.RefreshTokenRequest;
import com.innowise.authService.model.dto.request.ValidateTokenRequest;
import com.innowise.authService.model.dto.response.RegisterResponse;
import com.innowise.authService.model.dto.response.TokenResponse;
import com.innowise.authService.model.dto.response.ValidateTokenResponse;
import com.innowise.authService.model.entity.AuthUser;
import com.innowise.authService.repository.AuthUserRepository;
import com.innowise.authService.service.AuthService;
import com.innowise.authService.service.CustomUserDetailsService;
import com.innowise.authService.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthUserRepository authUserRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  public RegisterResponse createCredentials(CreateCredentialsRequest request) {
    if (authUserRepository.existsByUsername(request.getUsername())) {
      throw new CredentialsConflictException("username", request.getUsername());
    }
    if (authUserRepository.existsByEmail(request.getEmail())) {
      throw new CredentialsConflictException("email", request.getEmail());
    }

    AuthUser user = new AuthUser();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole());

    AuthUser savedUser = authUserRepository.save(user);
    log.info("Credentials created for username: {}", request.getUsername());
    return RegisterResponse.builder()
        .userId(savedUser.getId())
        .username(savedUser.getUsername())
        .email(savedUser.getEmail())
        .role(savedUser.getRole())
        .build();
  }

  @Override
  public TokenResponse createTokens(LoginRequest request) {
    if (authUserRepository.findByUsername(request.getUsername()).isEmpty()) {
      throw new AuthUserNotFoundException("username", request.getUsername());
    }

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      log.info("JWT tokens created for username: {}", userDetails.getUsername());
      return jwtService.generateTokens(userDetails);
    } catch (BadCredentialsException ex) {
      throw new LoginFailedException("Incorrect username or password");
    }
  }

  @Override
  public TokenResponse refreshTokens(RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();
    if (jwtService.isInvalid(refreshToken)) {
      throw new RefreshTokenRejectedException();
    }

    String username = jwtService.extractUsername(refreshToken);
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
    return jwtService.refreshTokens(refreshToken, userDetails);
  }

  @Override
  public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
    String token = request.getToken();
    if (jwtService.isInvalid(token)) {
      throw new AccessTokenRejectedException();
    }

    try {
      String username = jwtService.extractUsername(token);
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
      if (!(userDetails instanceof AuthUserDetails authUserDetails)) {
        throw new TokenValidationFailedException("Unsupported user details");
      }

      return ValidateTokenResponse.builder()
          .valid(true)
          .userId(authUserDetails.getUserId())
          .username(authUserDetails.getUsername())
          .email(authUserDetails.getEmail())
          .role(authUserDetails.getRole())
          .build();
    } catch (AuthUserNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new TokenValidationFailedException("Failed to validate token");
    }
  }
}
