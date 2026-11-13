package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.dto.UserCredentialsResponse;
import com.digitalmoneyhouse.auth.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class FeignUserDirectory implements UserDirectory {

    private final UserClient userClient;

    public FeignUserDirectory(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public UserCredentialsResponse findByEmail(String email) {
        try {
            return userClient.findByEmail(email);
        } catch (FeignException.NotFound exception) {
            throw new ResourceNotFoundException("El usuario no existe");
        }
    }
}
