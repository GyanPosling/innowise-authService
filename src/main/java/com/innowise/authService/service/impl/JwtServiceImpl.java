package com.innowise.authService.service.impl;

import com.innowise.authService.config.security.AuthUserDetails;
import com.innowise.authService.model.dto.response.TokenResponse;
import com.innowise.authService.model.entity.type.Role;
import com.innowise.authService.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long tokenExpiration;

  @Value("${jwt.refresh-expiration}")
  private Long refreshTokenExpiration;

  @Override
  public TokenResponse generateTokens(UserDetails userDetails) {
    return buildTokenResponse(
        userDetails,
        generateToken(userDetails, tokenExpiration),
        generateToken(userDetails, refreshTokenExpiration));
  }

  @Override
  public TokenResponse refreshTokens(String refreshToken, UserDetails userDetails) {
    return buildTokenResponse(
        userDetails,
        generateToken(userDetails, tokenExpiration),
        refreshToken);
  }

  @Override
  public boolean isInvalid(String token) {
    try {
      extractAllClaims(token);
      return false;
    } catch (ExpiredJwtException e) {
    } catch (MalformedJwtException | UnsupportedJwtException | SecurityException e) {
    } catch (IllegalArgumentException e) {
    } catch (Exception e) {
    }
    return true;
  }

  @Override
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  @Override
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  @Override
  public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("userId", Long.class));
  }

  @Override
  public Role extractRole(String token) {
    return extractClaim(token, claims -> Role.valueOf(claims.get("role", String.class)));
  }

  private String generateToken(UserDetails userDetails, long expirationMs) {
    Map<String, Object> claims = new HashMap<>();
    if (userDetails instanceof AuthUserDetails authUserDetails) {
      claims.put("userId", authUserDetails.getUserId());
      claims.put("role", authUserDetails.getRole().name());
    }

    long now = System.currentTimeMillis();
    Date expire = new Date(now + expirationMs);

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .addClaims(claims)
        .setIssuedAt(new Date(now))
        .setExpiration(expire)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private TokenResponse buildTokenResponse(
      UserDetails userDetails, String accessToken, String refreshToken) {
    TokenResponse.TokenResponseBuilder builder = TokenResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .username(userDetails.getUsername());

    if (userDetails instanceof AuthUserDetails authUserDetails) {
      builder.userId(authUserDetails.getUserId())
          .email(authUserDetails.getEmail())
          .role(authUserDetails.getRole());
    }

    return builder.build();
  }

  private <T> T extractClaim(String token, Function<Claims, T> function) {
    Claims claims = extractAllClaims(token);
    return function.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }
}
