package com.innowise.authservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.mapper.TokenResponseMapper;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceImplTest {

  private JwtServiceImpl jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtServiceImpl(new TokenResponseMapper());
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

    assertEquals(5L, jwtService.extractUserId(response.getAccessToken()));
    assertEquals(Role.USER, jwtService.extractRole(response.getAccessToken()));
  }

  @Test
  void validateToken_allowsValidToken() {
    AuthUser user = new AuthUser();
    user.setId(5L);
    user.setUsername("user");
    user.setEmail("user@example.com");
    user.setRole(Role.USER);
    AuthUserDetails userDetails = new AuthUserDetails(user);
    TokenResponse response = jwtService.generateTokens(userDetails);

    assertDoesNotThrow(() -> jwtService.validateToken(response.getAccessToken()));
  }

  @Test
  void validateToken_throwsForMalformedToken() {
    assertThrows(JwtException.class, () -> jwtService.validateToken("not-a-token"));
  }

  @Test
  void validateToken_throwsForExpiredToken() {
    String expiredToken = Jwts.builder()
        .setSubject("user")
        .setIssuedAt(new Date(System.currentTimeMillis() - 60000))
        .setExpiration(new Date(System.currentTimeMillis() - 1000))
        .signWith(Keys.hmacShaKeyFor(
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)),
            SignatureAlgorithm.HS256)
        .compact();

    assertThrows(JwtException.class, () -> jwtService.validateToken(expiredToken));
  }
}
