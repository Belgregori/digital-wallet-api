package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.client.UserDirectory;
import com.digitalmoneyhouse.auth.dto.LoginRequest;
import com.digitalmoneyhouse.auth.dto.LoginResponse;
import com.digitalmoneyhouse.auth.exception.UnauthorizedException;
import com.digitalmoneyhouse.auth.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "digital-money-house-auth-service-jwt-secret-key";
    private static final String INVALID_CREDENTIALS = "Email o contraseña incorrectos";

    @Mock
    private UserDirectory userDirectory;

    private TokenBlacklist tokenBlacklist;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenBlacklist = new TokenBlacklist();
        jwtService = new JwtService(SECRET, 60_000);
        authService = new AuthService(userDirectory, jwtService, tokenBlacklist);
    }

    @Test
    void loginReturnsJwtWhenCredentialsMatch() {
        when(userDirectory.authenticate("ana@example.com", "secreta12")).thenReturn("ana@example.com");

        LoginResponse response = authService.login(new LoginRequest(" Ana@Example.com ", "secreta12"));

        assertEquals("ana@example.com", jwtService.extractEmail(response.token()));
    }

    @Test
    void loginReturnsUnauthorizedWhenUserDoesNotExist() {
        when(userDirectory.authenticate("ana@example.com", "secreta12"))
                .thenThrow(new UnauthorizedException(INVALID_CREDENTIALS));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(new LoginRequest("ana@example.com", "secreta12")));

        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }

    @Test
    void loginReturnsUnauthorizedWhenPasswordDoesNotMatch() {
        when(userDirectory.authenticate("ana@example.com", "otraClave1"))
                .thenThrow(new UnauthorizedException(INVALID_CREDENTIALS));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.login(new LoginRequest("ana@example.com", "otraClave1")));

        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }

    @Test
    void loginReturnsBadRequestWhenEmailIsInvalid() {
        assertThrows(ValidationException.class, () -> authService.login(new LoginRequest("ana", "secreta12")));
        verifyNoInteractions(userDirectory);
    }

    @Test
    void loginReturnsBadRequestWhenBodyIsIncomplete() {
        assertThrows(ValidationException.class, () -> authService.login(new LoginRequest(" ", "")));
        verifyNoInteractions(userDirectory);
    }

    @Test
    void logoutStoresTokenInBlacklist() {
        String token = jwtService.generateToken("ana@example.com");

        authService.logout("Bearer " + token);

        assertTrue(tokenBlacklist.isBlacklisted(token));
    }

    @Test
    void logoutRejectsExpiredToken() {
        String token = new JwtService(SECRET, -60_000).generateToken("ana@example.com");

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authService.logout("Bearer " + token));

        assertEquals("El token está vencido", exception.getMessage());
        assertFalse(tokenBlacklist.isBlacklisted(token));
    }

    @Test
    void logoutRejectsMissingToken() {
        assertThrows(ValidationException.class, () -> authService.logout(null));
    }
}
