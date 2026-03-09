package com.innowise.authservice.controller.api;

import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegisterRequest;
import com.innowise.authservice.model.dto.request.ValidateTokenRequest;
import com.innowise.authservice.model.dto.response.RegisterResponse;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.dto.response.ValidateTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "Authentication and token API")
public interface AuthControllerApi {

  @Operation(summary = "Create credentials for user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Credentials created",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RegisterResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "409", description = "Username or email already exists")
  })
  ResponseEntity<RegisterResponse> register(RegisterRequest request);

  @Operation(summary = "Create access and refresh tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tokens created",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  ResponseEntity<TokenResponse> login(LoginRequest request);

  @Operation(summary = "Refresh access token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tokens refreshed",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = TokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Invalid refresh token")
  })
  ResponseEntity<TokenResponse> refreshToken(RefreshTokenRequest request);

  @Operation(summary = "Validate token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token is valid",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ValidateTokenResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Invalid token"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  ResponseEntity<ValidateTokenResponse> validateToken(ValidateTokenRequest request);
}
