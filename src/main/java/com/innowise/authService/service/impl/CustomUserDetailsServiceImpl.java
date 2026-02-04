package com.innowise.authService.service.impl;

import com.innowise.authService.config.security.AuthUserDetails;
import com.innowise.authService.exception.AuthUserNotFoundException;
import com.innowise.authService.model.entity.AuthUser;
import com.innowise.authService.repository.AuthUserRepository;
import com.innowise.authService.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

  private final AuthUserRepository authUserRepository;

  @Override
  public UserDetails loadUserByUsername(String username) {
    AuthUser user = authUserRepository.findByUsername(username)
        .orElseThrow(() -> new AuthUserNotFoundException("username", username));
    return new AuthUserDetails(user);
  }
}
