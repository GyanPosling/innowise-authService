package com.innowise.authservice.service.impl;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.CustomUserDetailsService;
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
