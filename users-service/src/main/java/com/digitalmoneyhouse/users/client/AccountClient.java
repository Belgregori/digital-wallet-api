package com.digitalmoneyhouse.users.client;

import com.digitalmoneyhouse.users.dto.AccountResponse;
import com.digitalmoneyhouse.users.dto.OpenAccountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service")
public interface AccountClient {

    @PostMapping("/internal/accounts")
    AccountResponse open(@RequestBody OpenAccountRequest request);
}
