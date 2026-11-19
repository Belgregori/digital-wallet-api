package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.dto.CredentialVerificationRequest;
import com.digitalmoneyhouse.auth.exception.UnauthorizedException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeignUserDirectoryTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private FeignUserDirectory userDirectory;

    @Test
    void mapsFeignUnauthorizedToGenericCredentialsError() {
        when(userClient.verify(new CredentialVerificationRequest("ana@example.com", "secreta12")))
                .thenThrow(unauthorized());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userDirectory.authenticate("ana@example.com", "secreta12"));

        assertEquals("Email o contraseña incorrectos", exception.getMessage());
    }

    private FeignException unauthorized() {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/internal/credentials/verify",
                Map.of(),
                null,
                new RequestTemplate());
        Response response = Response.builder()
                .status(401)
                .reason("Unauthorized")
                .request(request)
                .headers(Map.of())
                .body(new byte[0])
                .build();
        return FeignException.errorStatus("UserClient#verify", response);
    }
}
