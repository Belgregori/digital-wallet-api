package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.client.UserDirectory;
import com.digitalmoneyhouse.auth.dto.LoginRequest;
import com.digitalmoneyhouse.auth.dto.LoginResponse;
import com.digitalmoneyhouse.auth.dto.UserCredentialsResponse;
import com.digitalmoneyhouse.auth.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserDirectory userDirectory;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(UserDirectory userDirectory, JwtService jwtService, TokenBlacklist tokenBlacklist) {
        this.userDirectory = userDirectory;
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ValidationException("El email y la contraseña son obligatorios");
        }
        if (!request.email().contains("@")) {
            throw new ValidationException("El email no es válido");
        }

        String email = request.email().trim().toLowerCase();
        UserCredentialsResponse credentials = userDirectory.findByEmail(email);
        if (!passwordMatches(request.password(), credentials.password())) {
            throw new ValidationException("La contraseña es incorrecta");
        }
        return new LoginResponse(jwtService.generateToken(credentials.email()));
    }

    public void logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (!jwtService.hasValidSignature(token)) {
            throw new ValidationException("El token no es válido");
        }
        tokenBlacklist.invalidate(token);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ValidationException("El token es obligatorio");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new ValidationException("El token es obligatorio");
        }
        return token;
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }
}
