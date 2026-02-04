package com.innowise.authservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceImplTest {

  private JwtServiceImpl jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtServiceImpl();
    ReflectionTestUtils.setField(jwtService, "secret",
        "0123456789abcdef0123456789abcdef");
    ReflectionTestUtils.setField(jwtService, "tokenExpiration", 60000L);
    ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 120000L);
  }

  @Test
  void generateTokens_includesUserClaims() {
    AuthUser user = new AuthUser();
    user.setId(5L);
    user.setUsername("user");
    user.setEmail("user@example.com");
    user.setRole(Role.USER);
    AuthUserDetails userDetails = new AuthUserDetails(user);

    TokenResponse response = jwtService.generateTokens(userDetails);

    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(5L, response.getUserId());
    assertEquals("user", response.getUsername());
    assertEquals("user@example.com", response.getEmail());
    assertEquals(Role.USER, response.getRole());

    assertEquals(5L, jwtService.extractUserId(response.getAccessToken()));
    assertEquals(Role.USER, jwtService.extractRole(response.getAccessToken()));
  }

  @Test
  void isInvalid_returnsTrueForMalformedToken() {
    assertTrue(jwtService.isInvalid("not-a-token"));
  }
}
