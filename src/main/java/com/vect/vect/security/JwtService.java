package com.vect.vect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey key() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String email) {
        return generateToken(userId, email, accessExpirationMs, "access");
    }

    public String generateRefreshToken(UUID userId, String email) {
        return generateToken(userId, email, refreshExpirationMs, "refresh");
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("token_type", String.class))) {
            throw new IllegalArgumentException("Token is not an access token");
        }
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("token_type", String.class))) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }
        return claims;
    }

    private String generateToken(UUID userId, String email, long expirationMs, String tokenType) {
        return Jwts.builder()
            .subject(email)
            .id(userId.toString())
            .issuer(issuer)
            .claim("token_type", tokenType)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key())
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(key())
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getId());
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }
}
