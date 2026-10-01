package com.vect.vect.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtService, "issuer", "vect-tests");
        ReflectionTestUtils.setField(jwtService, "accessExpirationMs", 60_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpirationMs", 120_000L);
    }

    @Test
    void accessAndRefreshTokensCannotBeUsedInterchangeably() {
        UUID userId = UUID.randomUUID();
        String access = jwtService.generateAccessToken(userId, "test@example.com");
        String refresh = jwtService.generateRefreshToken(userId, "test@example.com");

        Claims accessClaims = jwtService.parseAccessToken(access);
        Claims refreshClaims = jwtService.parseRefreshToken(refresh);

        assertThat(accessClaims.get("token_type", String.class)).isEqualTo("access");
        assertThat(refreshClaims.get("token_type", String.class)).isEqualTo("refresh");
        assertThatThrownBy(() -> jwtService.parseAccessToken(refresh))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> jwtService.parseRefreshToken(access))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
