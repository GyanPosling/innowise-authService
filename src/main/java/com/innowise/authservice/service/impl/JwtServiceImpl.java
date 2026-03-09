package com.innowise.authservice.service.impl;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.mapper.TokenResponseMapper;
import com.innowise.authservice.model.dto.response.TokenResponse;
import com.innowise.authservice.model.entity.type.Role;
import com.innowise.authservice.model.entity.type.TokenType;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final TokenResponseMapper tokenResponseMapper;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long tokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    @Override
    public TokenResponse generateTokens(UserDetails userDetails) {
        return buildTokenResponse(
                generateToken(userDetails, tokenExpiration, TokenType.ACCESS),
                generateToken(userDetails, refreshTokenExpiration, TokenType.REFRESH));
    }

    @Override
    public TokenResponse refreshTokens(String refreshToken, UserDetails userDetails) {
        return buildTokenResponse(
                generateToken(userDetails, tokenExpiration, TokenType.ACCESS),
                refreshToken);
    }

    @Override
    public void validateToken(String token) {
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        if (expiration == null) {
            throw new JwtException("Token expiration is missing");
        }
        if (expiration.before(new Date())) {
            throw new JwtException("Token is expired");
        }
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (tokenType == null || tokenType.isBlank()) {
            throw new JwtException("Token type is missing");
        }
        try {
            TokenType.valueOf(tokenType);
        } catch (IllegalArgumentException ex) {
            throw new JwtException("Token type is invalid");
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public UUID extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", UUID.class));
    }

    @Override
    public Role extractRole(String token) {
        return extractClaim(token, claims -> Role.valueOf(claims.get("role", String.class)));
    }

    @Override
    public TokenType extractTokenType(String token) {
        return extractClaim(token, claims -> TokenType.valueOf(claims.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    private String generateToken(UserDetails userDetails, long expirationMs, TokenType tokenType) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof AuthUserDetails authUserDetails) {
            claims.put("userId", authUserDetails.getUserId());
            claims.put("role", authUserDetails.getRole().name());
        }
        claims.put(TOKEN_TYPE_CLAIM, tokenType.name());

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

    private TokenResponse buildTokenResponse(String accessToken, String refreshToken) {
        return tokenResponseMapper.toResponse(accessToken, refreshToken);
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
