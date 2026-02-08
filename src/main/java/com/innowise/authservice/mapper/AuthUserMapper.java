package com.innowise.authservice.mapper;

import com.innowise.authservice.model.dto.request.CreateCredentialsRequest;
import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.config.security.AuthUserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthUserMapper {

  public AuthUser toEntity(CreateCredentialsRequest request, String encodedPassword) {
    if (request == null) {
      return null;
    }
    AuthUser user = new AuthUser();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(encodedPassword);
    user.setRole(Role.USER);
    return user;
  }

  public RegisterResponse toRegisterResponse(AuthUser user) {
    if (user == null) {
      return null;
    }
    return RegisterResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole())
        .build();
  }

  public ValidateTokenResponse toValidateTokenResponse(AuthUserDetails userDetails) {
    if (userDetails == null) {
      return null;
    }
    return ValidateTokenResponse.builder()
        .valid(true)
        .userId(userDetails.getUserId())
        .username(userDetails.getUsername())
        .email(userDetails.getEmail())
        .role(userDetails.getRole())
        .build();
  }

  public PromoteUserResponse toPromoteUserResponse(AuthUser user) {
    if (user == null) {
      return null;
    }
    return PromoteUserResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .role(user.getRole())
        .build();
  }
}
