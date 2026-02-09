package com.innowise.authservice.mapper;

import com.innowise.authservice.model.dto.response.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenResponseMapper {

  public TokenResponse toResponse(String accessToken, String refreshToken) {
    return TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .build();
  }
}
