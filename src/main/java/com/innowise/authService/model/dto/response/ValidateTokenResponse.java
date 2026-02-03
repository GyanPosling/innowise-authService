package com.innowise.authService.model.dto.response;

import com.innowise.authService.model.entity.type.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTokenResponse {

  private boolean valid;
  private Long userId;
  private Role role;
}
