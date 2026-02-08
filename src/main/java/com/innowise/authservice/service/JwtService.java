package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.entity.type.Role;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

  TokenResponse generateTokens(UserDetails userDetails);

  TokenResponse refreshTokens(String refreshToken, UserDetails userDetails);

  void validateToken(String token);

  String extractUsername(String token);
  Long extractUserId(String token);

  Role extractRole(String token);
}
