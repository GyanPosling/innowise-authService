package com.innowise.authservice.service.impl;

import com.innowise.authservice.client.UserServiceClient;
import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.AccessTokenRejectedException;
import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.exception.CredentialsConflictException;
import com.innowise.authservice.exception.LoginFailedException;
import com.innowise.authservice.exception.RefreshTokenRejectedException;
import com.innowise.authservice.exception.TokenValidationFailedException;
import com.innowise.authservice.exception.UserServiceIntegrationException;
import com.innowise.authservice.mapper.AuthUserMapper;
import com.innowise.authservice.model.dto.request.CreateUserProfileRequest;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Service
@Slf4j
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
    if (authUserRepository.existsByUsername(request.getUsername())) {
      throw new CredentialsConflictException("username: " + request.getUsername());
    }
    if (authUserRepository.existsByEmail(request.getEmail())) {
      throw new CredentialsConflictException("email: " + request.getEmail());
    }

    UUID userId = UUID.randomUUID();
    String encodedPassword = passwordEncoder.encode(request.getPassword());
    AuthUser user = authUserMapper.toEntity(request, encodedPassword);
    user.setId(userId);

    AuthUser savedUser;
    try {
      savedUser = authUserRepository.save(user);
    } catch (DataIntegrityViolationException ex) {
      throw resolveCredentialsConflict(request, ex);
    }

    CreateUserProfileRequest createUserProfileRequest =
        authUserMapper.toCreateUserProfileRequest(request);
    createUserProfileRequest.setId(userId);

    try {
      userServiceClient.createUserProfile(createUserProfileRequest);
    } catch (HttpClientErrorException.Conflict ex) {
      rollbackAuthUser(userId, ex);
      throw new CredentialsConflictException("email: " + request.getEmail());
    } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException ex) {
      rollbackAuthUser(userId, ex);
      throw new UserServiceIntegrationException("Failed to create user profile", ex);
    }

    return authUserMapper.toRegisterResponse(savedUser);
  }

  @Override
  public TokenResponse createTokens(LoginRequest request) {
    if (authUserRepository.findByUsername(request.getUsername()).isEmpty()) {
      throw new AuthUserNotFoundException("username: " + request.getUsername());
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

  private RuntimeException resolveCredentialsConflict(RegisterRequest request, Exception ex) {
    if (authUserRepository.existsByUsername(request.getUsername())) {
      return new CredentialsConflictException("username: " + request.getUsername());
    }
    if (authUserRepository.existsByEmail(request.getEmail())) {
      return new CredentialsConflictException("email: " + request.getEmail());
    }
    return new UserServiceIntegrationException("Failed to save user credentials", ex);
  }

  private void rollbackAuthUser(UUID userId, Exception originalException) {
    try {
      authUserRepository.deleteById(userId);
    } catch (Exception rollbackException) {
      log.error(
          "Failed to rollback auth user {} after registration failure: {}",
          userId,
          rollbackException.getMessage(),
          rollbackException);
      log.error(
          "Original registration failure for {}: {}",
          userId,
          originalException.getMessage(),
          originalException);
    }
  }
}
