package com.innowise.authservice.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authservice.exception.GlobalExceptionHandler;
import com.innowise.authservice.model.dto.request.CreateCredentialsRequest;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.ValidateTokenRequest;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock
  private AuthService authService;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .build();
  }

  @Test
  void register_returnsCreated() throws Exception {
    CreateCredentialsRequest request = CreateCredentialsRequest.builder()
        .username("user")
        .email("user@example.com")
        .password("password123")
        .build();
    RegisterResponse response = RegisterResponse.builder()
        .userId(1L)
        .username("user")
        .email("user@example.com")
        .role(Role.USER)
        .build();
    when(authService.createCredentials(any(CreateCredentialsRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId", is(1)))
        .andExpect(jsonPath("$.username", is("user")))
        .andExpect(jsonPath("$.email", is("user@example.com")))
        .andExpect(jsonPath("$.role", is("USER")));
  }

  @Test
  void register_withInvalidPayload_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    verify(authService, never()).createCredentials(any(CreateCredentialsRequest.class));
  }

  @Test
  void login_returnsTokens() throws Exception {
    LoginRequest request = LoginRequest.builder()
        .username("user")
        .password("password123")
        .build();
    TokenResponse response = TokenResponse.builder()
        .accessToken("access")
        .refreshToken("refresh")
        .tokenType("Bearer")
        .build();
    when(authService.createTokens(any(LoginRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", is("access")))
        .andExpect(jsonPath("$.refreshToken", is("refresh")))
        .andExpect(jsonPath("$.tokenType", is("Bearer")));
  }

  @Test
  void login_withInvalidPayload_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    verify(authService, never()).createTokens(any(LoginRequest.class));
  }

  @Test
  void refresh_returnsTokens() throws Exception {
    RefreshTokenRequest request = RefreshTokenRequest.builder()
        .refreshToken("refresh")
        .build();
    TokenResponse response = TokenResponse.builder()
        .accessToken("access")
        .refreshToken("refresh")
        .tokenType("Bearer")
        .build();
    when(authService.refreshTokens(any(RefreshTokenRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken", is("access")))
        .andExpect(jsonPath("$.refreshToken", is("refresh")))
        .andExpect(jsonPath("$.tokenType", is("Bearer")));
  }

  @Test
  void refresh_withInvalidPayload_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    verify(authService, never()).refreshTokens(any(RefreshTokenRequest.class));
  }

  @Test
  void validate_returnsUserInfo() throws Exception {
    ValidateTokenRequest request = ValidateTokenRequest.builder()
        .token("access")
        .build();
    ValidateTokenResponse response = ValidateTokenResponse.builder()
        .valid(true)
        .userId(1L)
        .username("user")
        .email("user@example.com")
        .role(Role.USER)
        .build();
    when(authService.validateToken(any(ValidateTokenRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/v1/auth/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid", is(true)))
        .andExpect(jsonPath("$.userId", is(1)))
        .andExpect(jsonPath("$.username", is("user")))
        .andExpect(jsonPath("$.email", is("user@example.com")))
        .andExpect(jsonPath("$.role", is("USER")));
  }

  @Test
  void validate_withInvalidPayload_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/api/v1/auth/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    verify(authService, never()).validateToken(any(ValidateTokenRequest.class));
  }
}
