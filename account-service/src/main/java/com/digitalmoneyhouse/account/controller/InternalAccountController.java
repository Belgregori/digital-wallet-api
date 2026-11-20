package com.digitalmoneyhouse.account.controller;

import com.digitalmoneyhouse.account.dto.AccountDetailsResponse;
import com.digitalmoneyhouse.account.dto.OpenAccountRequest;
import com.digitalmoneyhouse.account.service.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountController {

    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountDetailsResponse open(@Valid @RequestBody OpenAccountRequest request) {
        return accountService.openAccount(request.userId(), request.email());
    }

    @GetMapping("/by-user/{userId}")
    public AccountDetailsResponse byUser(@PathVariable Long userId) {
        return accountService.findByUserId(userId);
    }
}
