package com.digitalmoneyhouse.users.controller;

import com.digitalmoneyhouse.users.exception.GlobalExceptionHandler;
import com.digitalmoneyhouse.users.security.InternalApiKeyFilter;
import com.digitalmoneyhouse.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalCredentialController.class)
@Import({GlobalExceptionHandler.class, InternalApiKeyFilter.class})
@TestPropertySource(properties = "internal.api-key=test-internal-key")
class InternalCredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void rejectsCallsWithoutInternalToken() throws Exception {
        mockMvc.perform(post("/internal/credentials/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"secreta12"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No autorizado"));

        verify(userService, never()).verifyCredentials("ana@example.com", "secreta12");
    }

    @Test
    void doesNotExposeTheLegacyCredentialLookup() throws Exception {
        mockMvc.perform(get("/internal/users").param("email", "ana@example.com")
                        .header(InternalApiKeyFilter.HEADER, "test-internal-key"))
                .andExpect(status().isNotFound());
    }

    @Test
    void verifiesCredentialsWhenTheInternalTokenMatches() throws Exception {
        when(userService.verifyCredentials("ana@example.com", "secreta12")).thenReturn("ana@example.com");

        mockMvc.perform(post("/internal/credentials/verify")
                        .header(InternalApiKeyFilter.HEADER, "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ana@example.com","password":"secreta12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
