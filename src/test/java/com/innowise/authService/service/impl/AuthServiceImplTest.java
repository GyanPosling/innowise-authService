package com.innowise.authService.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authService.config.security.AuthUserDetails;
import com.innowise.authService.exception.AccessTokenRejectedException;
import com.innowise.authService.exception.AuthUserNotFoundException;
import com.innowise.authService.exception.CredentialsConflictException;
import com.innowise.authService.exception.LoginFailedException;
import com.innowise.authService.exception.RefreshTokenRejectedException;
import com.innowise.authService.model.dto.request.CreateCredentialsRequest;
import com.innowise.authService.model.dto.request.LoginRequest;
import com.innowise.authService.model.dto.request.RefreshTokenRequest;
import com.innowise.authService.model.dto.request.ValidateTokenRequest;
import com.innowise.authService.model.dto.response.RegisterResponse;
import com.innowise.authService.model.dto.response.TokenResponse;
import com.innowise.authService.model.dto.response.ValidateTokenResponse;
import com.innowise.authService.model.entity.AuthUser;
import com.innowise.authService.model.entity.type.Role;
import com.innowise.authService.repository.AuthUserRepository;
import com.innowise.authService.service.CustomUserDetailsService;
import com.innowise.authService.service.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private AuthUserRepository authUserRepository;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private JwtService jwtService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private CustomUserDetailsService customUserDetailsService;
  @Mock
  private Authentication authentication;

  @InjectMocks
  private AuthServiceImpl authService;

  @Test
  void createCredentials_whenUsernameExists_throwsConflict() {
    CreateCredentialsRequest request = CreateCredentialsRequest.builder()
        .username("user")
        .email("user@example.com")
        .password("password123")
        .role(Role.USER)
        .build();
    when(authUserRepository.existsByUsername("user")).thenReturn(true);

    assertThrows(CredentialsConflictException.class, () -> authService.createCredentials(request));
  }

  @Test
  void createCredentials_whenEmailExists_throwsConflict() {
    CreateCredentialsRequest request = CreateCredentialsRequest.builder()
        .username("user")
        .email("user@example.com")
        .password("password123")
        .role(Role.USER)
        .build();
    when(authUserRepository.existsByUsername("user")).thenReturn(false);
    when(authUserRepository.existsByEmail("user@example.com")).thenReturn(true);

    assertThrows(CredentialsConflictException.class, () -> authService.createCredentials(request));
  }

  @Test
  void createCredentials_whenValid_savesUserAndReturnsResponse() {
    CreateCredentialsRequest request = CreateCredentialsRequest.builder()
        .username("user")
        .email("user@example.com")
        .password("password123")
        .role(Role.USER)
        .build();
    when(passwordEncoder.encode("password123")).thenReturn("encoded");
    when(authUserRepository.save(any(AuthUser.class))).thenAnswer(invocation -> {
      AuthUser user = invocation.getArgument(0);
      user.setId(10L);
      return user;
    });

    RegisterResponse response = authService.createCredentials(request);

    ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
    verify(authUserRepository).save(captor.capture());
    AuthUser saved = captor.getValue();
    assertEquals("user", saved.getUsername());
    assertEquals("user@example.com", saved.getEmail());
    assertEquals("encoded", saved.getPassword());
    assertEquals(Role.USER, saved.getRole());
    assertEquals(10L, response.getUserId());
    assertEquals("user", response.getUsername());
    assertEquals("user@example.com", response.getEmail());
    assertEquals(Role.USER, response.getRole());
  }

  @Test
  void createTokens_whenUserNotFound_throwsNotFound() {
    LoginRequest request = new LoginRequest("user", "password123");
    when(authUserRepository.findByUsername("user")).thenReturn(Optional.empty());

    assertThrows(AuthUserNotFoundException.class, () -> authService.createTokens(request));
  }

  @Test
  void createTokens_whenBadCredentials_throwsLoginFailed() {
    LoginRequest request = new LoginRequest("user", "password123");
    when(authUserRepository.findByUsername("user")).thenReturn(Optional.of(new AuthUser()));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("bad credentials"));

    assertThrows(LoginFailedException.class, () -> authService.createTokens(request));
  }

  @Test
  void createTokens_whenAuthenticated_returnsTokens() {
    LoginRequest request = new LoginRequest("user", "password123");
    UserDetails userDetails = new User("user", "pass", new java.util.ArrayList<>());
    TokenResponse expected = TokenResponse.builder().accessToken("access").refreshToken("refresh")
        .tokenType("Bearer").username("user").build();
    when(authUserRepository.findByUsername("user")).thenReturn(Optional.of(new AuthUser()));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtService.generateTokens(userDetails)).thenReturn(expected);

    TokenResponse response = authService.createTokens(request);

    assertSame(expected, response);
  }

  @Test
  void refreshTokens_whenInvalid_throwsRejected() {
    RefreshTokenRequest request = new RefreshTokenRequest("bad-token");
    when(jwtService.isInvalid("bad-token")).thenReturn(true);

    assertThrows(RefreshTokenRejectedException.class, () -> authService.refreshTokens(request));
  }

  @Test
  void refreshTokens_whenValid_returnsTokens() {
    RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
    UserDetails userDetails = new User("user", "pass", new java.util.ArrayList<>());
    TokenResponse expected = TokenResponse.builder().accessToken("access").refreshToken("refresh-token")
        .tokenType("Bearer").username("user").build();
    when(jwtService.isInvalid("refresh-token")).thenReturn(false);
    when(jwtService.extractUsername("refresh-token")).thenReturn("user");
    when(customUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
    when(jwtService.refreshTokens("refresh-token", userDetails)).thenReturn(expected);

    TokenResponse response = authService.refreshTokens(request);

    assertSame(expected, response);
  }

  @Test
  void validateToken_whenInvalid_throwsRejected() {
    ValidateTokenRequest request = new ValidateTokenRequest("bad-token");
    when(jwtService.isInvalid("bad-token")).thenReturn(true);

    assertThrows(AccessTokenRejectedException.class, () -> authService.validateToken(request));
  }

  @Test
  void validateToken_whenValid_returnsUserInfo() {
    ValidateTokenRequest request = new ValidateTokenRequest("token");
    AuthUser user = new AuthUser();
    user.setId(7L);
    user.setUsername("user");
    user.setEmail("user@example.com");
    user.setRole(Role.ADMIN);
    AuthUserDetails userDetails = new AuthUserDetails(user);
    when(jwtService.isInvalid("token")).thenReturn(false);
    when(jwtService.extractUsername("token")).thenReturn("user");
    when(customUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);

    ValidateTokenResponse response = authService.validateToken(request);

    assertTrue(response.isValid());
    assertEquals(7L, response.getUserId());
    assertEquals("user", response.getUsername());
    assertEquals("user@example.com", response.getEmail());
    assertEquals(Role.ADMIN, response.getRole());
  }
}
