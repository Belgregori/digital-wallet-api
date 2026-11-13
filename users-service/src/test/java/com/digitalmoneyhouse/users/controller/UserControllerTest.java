package com.digitalmoneyhouse.users.controller;

import com.digitalmoneyhouse.users.dto.AccountResponse;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.exception.GlobalExceptionHandler;
import com.digitalmoneyhouse.users.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.users.exception.UnauthorizedException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
                                  "password": "secreta"
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

    private String validBody() {
        return """
                {
                  "firstName": "Ana",
                  "lastName": "Perez",
                  "email": "ana@example.com",
                  "dni": "12345678",
                  "phoneNumber": "1122334455",
                  "password": "secreta"
                }
                """;
    }
}
