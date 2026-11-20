package com.digitalmoneyhouse.users.client;

import com.digitalmoneyhouse.users.dto.AccountResponse;
import com.digitalmoneyhouse.users.dto.OpenAccountRequest;
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
}
