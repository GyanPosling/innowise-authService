package com.innowise.authservice.model.dto.response;

import com.innowise.authservice.model.entity.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private Long userId;
  private String username;
  private String email;
  private Role role;
}
