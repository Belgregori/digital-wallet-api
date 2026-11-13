package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.client.UserDirectory;
import com.digitalmoneyhouse.auth.dto.LoginRequest;
import com.digitalmoneyhouse.auth.dto.LoginResponse;
import com.digitalmoneyhouse.auth.dto.UserCredentialsResponse;
import com.digitalmoneyhouse.auth.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.auth.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET = "digital-money-house-auth-service-jwt-secret-key";

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
        when(userDirectory.findByEmail("ana@example.com"))
                .thenReturn(new UserCredentialsResponse("ana@example.com", "secreta"));

        LoginResponse response = authService.login(new LoginRequest(" Ana@Example.com ", "secreta"));

        assertEquals("ana@example.com", jwtService.extractEmail(response.token()));
    }

    @Test
    void loginReturnsNotFoundWhenUserDoesNotExist() {
        when(userDirectory.findByEmail("ana@example.com"))
                .thenThrow(new ResourceNotFoundException("El usuario no existe"));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(new LoginRequest("ana@example.com", "secreta")));

        assertEquals("El usuario no existe", exception.getMessage());
    }

    @Test
    void loginReturnsBadRequestWhenPasswordDoesNotMatch() {
        when(userDirectory.findByEmail("ana@example.com"))
                .thenReturn(new UserCredentialsResponse("ana@example.com", "secreta"));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.login(new LoginRequest("ana@example.com", "otra")));

        assertEquals("La contraseña es incorrecta", exception.getMessage());
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
    void logoutRejectsMissingToken() {
        assertThrows(ValidationException.class, () -> authService.logout(null));
    }
}
