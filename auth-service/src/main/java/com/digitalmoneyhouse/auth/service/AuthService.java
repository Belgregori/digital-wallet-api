package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.client.UserDirectory;
import com.digitalmoneyhouse.auth.dto.LoginRequest;
import com.digitalmoneyhouse.auth.dto.LoginResponse;
import com.digitalmoneyhouse.auth.exception.UnauthorizedException;
import com.digitalmoneyhouse.auth.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

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
        if (request.password().length() < 8 || request.password().length() > 72) {
            throw new ValidationException("La contraseña debe tener entre 8 y 72 caracteres");
        }
        String email = request.email().trim().toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("El email no es válido");
        }

        String verifiedEmail = userDirectory.authenticate(email, request.password());
        return new LoginResponse(jwtService.generateToken(verifiedEmail));
    }

    public void logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        switch (jwtService.inspect(token)) {
            case VALID -> tokenBlacklist.invalidate(token);
            case EXPIRED -> throw new UnauthorizedException("El token está vencido");
            case INVALID -> throw new UnauthorizedException("El token no es válido");
        }
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
}
