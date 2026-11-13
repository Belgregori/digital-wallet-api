package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.exception.ResourceNotFoundException;
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
    void mapsFeignNotFoundToResourceNotFound() {
        when(userClient.findByEmail("ana@example.com")).thenThrow(notFound());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userDirectory.findByEmail("ana@example.com"));

        assertEquals("El usuario no existe", exception.getMessage());
    }

    private FeignException notFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/internal/users",
                Map.of(),
                null,
                new RequestTemplate());
        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Map.of())
                .body(new byte[0])
                .build();
        return FeignException.errorStatus("UserClient#findByEmail", response);
    }
}
