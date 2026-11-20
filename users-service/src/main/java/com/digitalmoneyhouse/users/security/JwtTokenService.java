package com.digitalmoneyhouse.users.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtTokenService {

    public enum TokenStatus {
        VALID,
        EXPIRED,
        INVALID
    }

    private final Key key;

    public JwtTokenService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public TokenStatus inspect(String token) {
        try {
            parse(token);
            return TokenStatus.VALID;
        } catch (ExpiredJwtException exception) {
            return TokenStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException exception) {
            return TokenStatus.INVALID;
        }
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
