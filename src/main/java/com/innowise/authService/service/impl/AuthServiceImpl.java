package com.innowise.authService.service.impl;

import com.innowise.authService.config.security.AuthUserDetails;
import com.innowise.authService.exception.AccessTokenRejectedException;
import com.innowise.authService.exception.AuthUserNotFoundException;
import com.innowise.authService.exception.CredentialsConflictException;
import com.innowise.authService.exception.LoginFailedException;
import com.innowise.authService.exception.RefreshTokenRejectedException;
import com.innowise.authService.exception.TokenValidationFailedException;
import com.innowise.authService.mapper.AuthUserMapper;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

  @Override
  public RegisterResponse createCredentials(CreateCredentialsRequest request) {
    if (authUserRepository.existsByUsername(request.getUsername())) {
      throw new CredentialsConflictException("username", request.getUsername());
    }
    if (authUserRepository.existsByEmail(request.getEmail())) {
      throw new CredentialsConflictException("email", request.getEmail());
    }

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    AuthUser user = authUserMapper.toEntity(request, encodedPassword);
    AuthUser savedUser = authUserRepository.save(user);
    return authUserMapper.toRegisterResponse(savedUser);
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

      return authUserMapper.toValidateTokenResponse(authUserDetails);
    } catch (AuthUserNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new TokenValidationFailedException("Failed to validate token");
    }
  }
}
