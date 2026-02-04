package com.innowise.authService.config.security;

import com.innowise.authService.model.entity.AuthUser;
import com.innowise.authService.model.entity.type.Role;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUserDetails implements UserDetails {

  private final AuthUser user;

  public AuthUserDetails(AuthUser user) {
    this.user = user;
  }

  public Long getUserId() {
    return user.getId();
  }

  public Role getRole() {
    return user.getRole();
  }

  public String getEmail() {
    return user.getEmail();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return user.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return user.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }
}
