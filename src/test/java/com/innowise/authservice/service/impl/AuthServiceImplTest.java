package com.innowise.authservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.AccessTokenRejectedException;
import com.innowise.authservice.exception.AuthUserNotFoundException;
import com.innowise.authservice.exception.CredentialsConflictException;
import com.innowise.authservice.exception.LoginFailedException;
import com.innowise.authservice.exception.RefreshTokenRejectedException;
import com.innowise.authservice.mapper.AuthUserMapper;
import com.innowise.authservice.model.dto.request.CreateCredentialsRequest;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.ValidateTokenRequest;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.CustomUserDetailsService;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  private AuthUserMapper authUserMapper;
  @Mock
  private Authentication authentication;

  @InjectMocks
  private com.innowise.authservice.service.impl.AuthServiceImpl authService;

  @Test
  void createCredentials_whenUsernameExists_throwsConflict() {
    CreateCredentialsRequest request = CreateCredentialsRequest.builder()
        .username("user")
        .email("user@example.com")
        .password("password123")
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
        .build();
    AuthUser mappedUser = new AuthUser();
    mappedUser.setUsername("user");
    mappedUser.setEmail("user@example.com");
    mappedUser.setPassword("encoded");
    mappedUser.setRole(Role.USER);
    AuthUser savedUser = new AuthUser();
    savedUser.setId(10L);
    savedUser.setUsername("user");
    savedUser.setEmail("user@example.com");
    savedUser.setPassword("encoded");
    savedUser.setRole(Role.USER);
    RegisterResponse expected = RegisterResponse.builder()
        .userId(10L)
        .username("user")
        .email("user@example.com")
        .role(Role.USER)
        .build();
    when(passwordEncoder.encode("password123")).thenReturn("encoded");
    when(authUserMapper.toEntity(request, "encoded")).thenReturn(mappedUser);
    when(authUserRepository.save(mappedUser)).thenReturn(savedUser);
    when(authUserMapper.toRegisterResponse(savedUser)).thenReturn(expected);

    RegisterResponse response = authService.createCredentials(request);

    verify(authUserMapper).toEntity(request, "encoded");
    verify(authUserRepository).save(mappedUser);
    verify(authUserMapper).toRegisterResponse(savedUser);
    assertSame(expected, response);
  }

  @Test
  void createTokens_whenUserNotFound_throwsNotFound() {
    LoginRequest request = LoginRequest.builder()
        .username("user")
        .password("password123")
        .build();
    when(authUserRepository.findByUsername("user")).thenReturn(Optional.empty());

    assertThrows(AuthUserNotFoundException.class, () -> authService.createTokens(request));
  }

  @Test
  void createTokens_whenBadCredentials_throwsLoginFailed() {
    LoginRequest request = LoginRequest.builder()
        .username("user")
        .password("password123")
        .build();
    when(authUserRepository.findByUsername("user")).thenReturn(Optional.of(new AuthUser()));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("bad credentials"));

    assertThrows(LoginFailedException.class, () -> authService.createTokens(request));
  }

  @Test
  void createTokens_whenAuthenticated_returnsTokens() {
    LoginRequest request = LoginRequest.builder()
        .username("user")
        .password("password123")
        .build();
    UserDetails userDetails = new User("user", "pass", new java.util.ArrayList<>());
    TokenResponse expected = TokenResponse.builder().accessToken("access").refreshToken("refresh")
        .tokenType("Bearer").build();
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
    RefreshTokenRequest request = RefreshTokenRequest.builder()
        .refreshToken("bad-token")
        .build();
    doThrow(new JwtException("invalid token")).when(jwtService).validateToken("bad-token");

    assertThrows(RefreshTokenRejectedException.class, () -> authService.refreshTokens(request));
  }

  @Test
  void refreshTokens_whenValid_returnsTokens() {
    RefreshTokenRequest request = RefreshTokenRequest.builder()
        .refreshToken("refresh-token")
        .build();
    UserDetails userDetails = new User("user", "pass", new java.util.ArrayList<>());
    TokenResponse expected = TokenResponse.builder().accessToken("access").refreshToken("refresh-token")
        .tokenType("Bearer").build();
    when(jwtService.extractUsername("refresh-token")).thenReturn("user");
    when(customUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
    when(jwtService.refreshTokens("refresh-token", userDetails)).thenReturn(expected);

    TokenResponse response = authService.refreshTokens(request);

    assertSame(expected, response);
  }

  @Test
  void validateToken_whenInvalid_throwsRejected() {
    ValidateTokenRequest request = ValidateTokenRequest.builder()
        .token("bad-token")
        .build();
    doThrow(new JwtException("invalid token")).when(jwtService).validateToken("bad-token");

    assertThrows(AccessTokenRejectedException.class, () -> authService.validateToken(request));
  }

  @Test
  void validateToken_whenValid_returnsUserInfo() {
    ValidateTokenRequest request = ValidateTokenRequest.builder()
        .token("token")
        .build();
    AuthUser user = new AuthUser();
    user.setId(7L);
    user.setUsername("user");
    user.setEmail("user@example.com");
    user.setRole(Role.ADMIN);
    AuthUserDetails userDetails = new AuthUserDetails(user);
    ValidateTokenResponse expected = ValidateTokenResponse.builder()
        .valid(true)
        .userId(7L)
        .username("user")
        .email("user@example.com")
        .role(Role.ADMIN)
        .build();
    when(jwtService.extractUsername("token")).thenReturn("user");
    when(customUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
    when(authUserMapper.toValidateTokenResponse(userDetails)).thenReturn(expected);

    ValidateTokenResponse response = authService.validateToken(request);

    verify(authUserMapper).toValidateTokenResponse(userDetails);
    assertSame(expected, response);
  }
}
