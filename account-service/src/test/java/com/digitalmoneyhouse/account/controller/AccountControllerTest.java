package com.digitalmoneyhouse.account.controller;

import com.digitalmoneyhouse.account.dto.AccountDetailsResponse;
import com.digitalmoneyhouse.account.dto.TransactionResponse;
import com.digitalmoneyhouse.account.exception.ForbiddenException;
import com.digitalmoneyhouse.account.exception.GlobalExceptionHandler;
import com.digitalmoneyhouse.account.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.account.security.CurrentUser;
import com.digitalmoneyhouse.account.security.JwtTokenService;
import com.digitalmoneyhouse.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = {
        "internal.api-key=test-internal-key",
        "jwt.secret=digital-money-house-auth-service-jwt-secret-key"
})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CurrentUser currentUser;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Test
    void returnsAvailableBalance() throws Exception {
        when(currentUser.email()).thenReturn("ana@example.com");
        when(accountService.getById(8L, "ana@example.com")).thenReturn(
                new AccountDetailsResponse(8L, 4L, "1234567890123456789012", "sol.luna.rio", new BigDecimal("150.50")));

        mockMvc.perform(get("/accounts/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.50))
                .andExpect(jsonPath("$.cvu").value("1234567890123456789012"));
    }

    @Test
    void returnsTransactionsFromNewestToOldest() throws Exception {
        when(currentUser.email()).thenReturn("ana@example.com");
        when(accountService.listTransactions(8L, "ana@example.com", 5)).thenReturn(List.of(
                new TransactionResponse(3L, 8L, new BigDecimal("20.00"), Instant.parse("2026-11-19T15:00:00Z"), "nueva"),
                new TransactionResponse(1L, 8L, new BigDecimal("10.00"), Instant.parse("2026-11-19T10:00:00Z"), "vieja")));

        mockMvc.perform(get("/accounts/8/transactions").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("nueva"))
                .andExpect(jsonPath("$[1].description").value("vieja"));
    }

    @Test
    void returns404WhenAccountDoesNotExist() throws Exception {
        when(currentUser.email()).thenReturn("ana@example.com");
        when(accountService.getById(8L, "ana@example.com"))
                .thenThrow(new ResourceNotFoundException("La cuenta no existe"));

        mockMvc.perform(get("/accounts/8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returns403WhenUserDoesNotOwnTheAccount() throws Exception {
        when(currentUser.email()).thenReturn("otro@example.com");
        when(accountService.getById(8L, "otro@example.com"))
                .thenThrow(new ForbiddenException("No tenés permiso para ver esta cuenta"));

        mockMvc.perform(get("/accounts/8"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void returns400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/accounts/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
