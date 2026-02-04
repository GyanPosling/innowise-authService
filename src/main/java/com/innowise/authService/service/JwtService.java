package com.innowise.authService.service;

import com.innowise.authService.model.dto.response.TokenResponse;
import com.innowise.authService.model.entity.type.Role;
import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

  TokenResponse generateTokens(UserDetails userDetails);

  TokenResponse refreshTokens(String refreshToken, UserDetails userDetails);

  boolean isInvalid(String token);

  String extractUsername(String token);

  Date extractExpiration(String token);

  Long extractUserId(String token);

  Role extractRole(String token);
}
