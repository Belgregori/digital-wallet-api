package com.digitalmoneyhouse.account.controller;

import com.digitalmoneyhouse.account.dto.AccountDetailsResponse;
import com.digitalmoneyhouse.account.dto.AliasUpdateRequest;
import com.digitalmoneyhouse.account.dto.TransactionResponse;
import com.digitalmoneyhouse.account.security.CurrentUser;
import com.digitalmoneyhouse.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> transactions(
            @PathVariable Long id,
            @RequestParam(required = false) Integer limit) {
        return accountService.listTransactions(id, currentUser.email(), limit);
    }

    @PatchMapping("/{id}")
    public AccountDetailsResponse updateAlias(@PathVariable Long id, @Valid @RequestBody AliasUpdateRequest request) {
        return accountService.updateAlias(id, currentUser.email(), request.alias());
    }
}
