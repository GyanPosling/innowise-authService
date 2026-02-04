package com.innowise.authService.controller;

import com.innowise.authService.model.dto.request.CreateCredentialsRequest;
import com.innowise.authService.model.dto.request.LoginRequest;
import com.innowise.authService.model.dto.request.RefreshTokenRequest;
import com.innowise.authService.model.dto.request.ValidateTokenRequest;
import com.innowise.authService.model.dto.response.RegisterResponse;
import com.innowise.authService.model.dto.response.TokenResponse;
import com.innowise.authService.model.dto.response.ValidateTokenResponse;
import com.innowise.authService.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication and token API")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "Create credentials for user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Credentials created",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RegisterResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "409", description = "Username or email already exists")
  })
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Valid @RequestBody CreateCredentialsRequest request) {
    RegisterResponse response = authService.createCredentials(request);
    log.info("Credentials created for username: {}", request.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "Create access and refresh tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tokens created",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    TokenResponse response = authService.createTokens(request);
    log.info("Tokens created for username: {}", request.getUsername());
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Refresh access token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tokens refreshed",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Invalid refresh token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    TokenResponse response = authService.refreshTokens(request);
    log.info("Tokens refreshed");
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Validate token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token is valid",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ValidateTokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Invalid token"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @PostMapping("/validate")
  public ResponseEntity<ValidateTokenResponse> validateToken(
      @Valid @RequestBody ValidateTokenRequest request) {
    ValidateTokenResponse response = authService.validateToken(request);
    log.info("Token validated for userId: {}", response.getUserId());
    return ResponseEntity.ok(response);
  }
}
