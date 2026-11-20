package com.digitalmoneyhouse.account.controller;

import com.digitalmoneyhouse.account.dto.AccountDetailsResponse;
import com.digitalmoneyhouse.account.security.CurrentUser;
import com.digitalmoneyhouse.account.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final CurrentUser currentUser;

    public AccountController(AccountService accountService, CurrentUser currentUser) {
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    @GetMapping("/{id}")
    public AccountDetailsResponse getAccount(@PathVariable Long id) {
        return accountService.getById(id, currentUser.email());
    }
}
