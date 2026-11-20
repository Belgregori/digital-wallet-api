package com.digitalmoneyhouse.users.controller;

import com.digitalmoneyhouse.users.dto.AccountResponse;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.exception.GlobalExceptionHandler;
import com.digitalmoneyhouse.users.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.users.exception.UnauthorizedException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.dto.UserProfileResponse;
import com.digitalmoneyhouse.users.security.CurrentUser;
import com.digitalmoneyhouse.users.security.JwtTokenService;
import com.digitalmoneyhouse.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "internal.api-key=test-internal-key",
        "jwt.secret=digital-money-house-auth-service-jwt-secret-key"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUser currentUser;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    void registerReturnsCreatedWithoutPassword() throws Exception {
        when(userService.register(any())).thenReturn(new UserResponse(
                1L,
                "Ana",
                "Perez",
                "12345678",
                "ana@example.com",
                "1122334455",
                new AccountResponse(2L, "1234567890123456789012", "sol.luna.rio", new BigDecimal("0.00"))
        ));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ana",
                                  "lastName": "Perez",
                                  "email": "ana@example.com",
                                  "dni": "12345678",
                                  "phoneNumber": "1122334455",
                                  "password": "secreta12"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.account.cvu").value("1234567890123456789012"))
                .andExpect(jsonPath("$.account.alias").value("sol.luna.rio"))
                .andExpect(jsonPath("$.account.balance").value(0));
    }

    @Test
    void profileReturnsPersonalDataWithCvuAndAlias() throws Exception {
        when(currentUser.email()).thenReturn("ana@example.com");
        when(userService.profile(1L, "ana@example.com")).thenReturn(new UserProfileResponse(
                1L, "Ana", "Perez", "12345678", "ana@example.com", "1122334455",
                "1234567890123456789012", "sol.luna.rio"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvu").value("1234567890123456789012"))
                .andExpect(jsonPath("$.alias").value("sol.luna.rio"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void updateChangesPersonalData() throws Exception {
        when(currentUser.email()).thenReturn("ana@example.com");
        when(userService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("ana@example.com"), any()))
                .thenReturn(new UserProfileResponse(
                        1L, "Ana Maria", "Perez", "12345678", "ana@example.com", "1199887766",
                        "1234567890123456789012", "sol.luna.rio"));

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ana Maria","lastName":"Perez","phoneNumber":"1199887766"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Maria"))
                .andExpect(jsonPath("$.phoneNumber").value("1199887766"));
    }

    @Test
    void registerReturnsBadRequestWhenEmailIsDuplicated() throws Exception {
        when(userService.register(any())).thenThrow(new ValidationException("El email ya está registrado"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));
    }

    @Test
    void registerReturnsBadRequestWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "Perez",
                                  "email": "no-es-email",
                                  "dni": "12",
                                  "phoneNumber": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void notFoundReturns404() throws Exception {
        when(userService.register(any())).thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    @Test
    void unauthorizedReturns401() throws Exception {
        when(userService.register(any())).thenThrow(new UnauthorizedException("No autorizado"));

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("No autorizado"));
    }

    @Test
    void registerReturnsBadRequestWhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Ana",
                                  "lastName": "Perez",
                                  "email": "ana@example.com",
                                  "dni": "12345678",
                                  "phoneNumber": "1122334455",
                                  "password": "corta"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")));
    }

    private String validBody() {
        return """
                {
                  "firstName": "Ana",
                  "lastName": "Perez",
                  "email": "ana@example.com",
                  "dni": "12345678",
                  "phoneNumber": "1122334455",
                  "password": "secreta12"
                }
                """;
    }
}
