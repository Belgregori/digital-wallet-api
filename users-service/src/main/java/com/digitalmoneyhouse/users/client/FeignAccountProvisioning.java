package com.digitalmoneyhouse.users.client;

import com.digitalmoneyhouse.users.dto.AccountResponse;
import com.digitalmoneyhouse.users.dto.OpenAccountRequest;
import com.digitalmoneyhouse.users.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class FeignAccountProvisioning implements AccountProvisioning {

    private final AccountClient accountClient;

    public FeignAccountProvisioning(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @Override
    public AccountResponse openAccount(Long userId, String email) {
        return accountClient.open(new OpenAccountRequest(userId, email));
    }

    @Override
    public AccountResponse findByUserId(Long userId) {
        try {
            return accountClient.findByUserId(userId);
        } catch (FeignException.NotFound exception) {
            throw new ResourceNotFoundException("La cuenta no existe");
        }
    }
}
