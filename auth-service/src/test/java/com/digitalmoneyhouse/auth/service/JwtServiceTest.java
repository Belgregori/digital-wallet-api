package com.digitalmoneyhouse.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "digital-money-house-auth-service-jwt-secret-key";

    @Test
    void expiredTokenCannotBeUsed() {
        JwtService jwtService = new JwtService(SECRET, -60_000);
        String token = jwtService.generateToken("ana@example.com");

        assertEquals(JwtService.TokenStatus.EXPIRED, jwtService.inspect(token));
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtService.extractEmail(token));
    }

    @Test
    void tamperedTokenIsInvalid() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String token = jwtService.generateToken("ana@example.com") + "x";

        assertEquals(JwtService.TokenStatus.INVALID, jwtService.inspect(token));
    }
}
