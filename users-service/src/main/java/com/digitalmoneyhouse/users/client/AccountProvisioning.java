package com.digitalmoneyhouse.users.client;

import com.digitalmoneyhouse.users.dto.AccountResponse;

public interface AccountProvisioning {

    AccountResponse openAccount(Long userId, String email);

    AccountResponse findByUserId(Long userId);
}
