package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.dto.CredentialVerificationRequest;
import com.digitalmoneyhouse.auth.exception.UnauthorizedException;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class FeignUserDirectory implements UserDirectory {

    static final String INVALID_CREDENTIALS = "Email o contraseña incorrectos";

    private final UserClient userClient;

    public FeignUserDirectory(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public String authenticate(String email, String password) {
        try {
            return userClient.verify(new CredentialVerificationRequest(email, password)).email();
        } catch (FeignException.Unauthorized | FeignException.NotFound exception) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
    }
}
