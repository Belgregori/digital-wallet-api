package com.digitalmoneyhouse.auth.controller;

import com.digitalmoneyhouse.auth.dto.LoginResponse;
import com.digitalmoneyhouse.auth.exception.GlobalExceptionHandler;
import com.digitalmoneyhouse.auth.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.auth.exception.ValidationException;
import com.digitalmoneyhouse.auth.service.AuthService;
import com.digitalmoneyhouse.auth.service.JwtService;
import com.digitalmoneyhouse.auth.service.TokenBlacklist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {AuthController.class, LogoutController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklist tokenBlacklist;

    @Test
    void loginReturnsToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"secreta"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginReturns404WhenUserDoesNotExist() throws Exception {
        when(authService.login(any())).thenThrow(new ResourceNotFoundException("El usuario no existe"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"secreta"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("El usuario no existe"));
    }

    @Test
    void loginReturns400WhenPasswordIsWrong() throws Exception {
        when(authService.login(any())).thenThrow(new ValidationException("La contraseña es incorrecta"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"otra"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void logoutReturns200() throws Exception {
        mockMvc.perform(post("/user/logout").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada"));
    }

    @Test
    void logoutReturns400WithoutToken() throws Exception {
        doThrow(new ValidationException("El token es obligatorio")).when(authService).logout(null);

        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
