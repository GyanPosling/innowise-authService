package com.innowise.authservice.model.dto.response;

import com.innowise.authservice.model.entity.type.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

  private UUID userId;
  private String username;
  private String email;
  private Role role;
  private String accessToken;
  private String refreshToken;
  private String tokenType;
}
