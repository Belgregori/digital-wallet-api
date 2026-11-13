package com.digitalmoneyhouse.users.dto;

import com.digitalmoneyhouse.users.entity.Account;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String cvu,
        String alias,
        BigDecimal balance
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCvu(),
                account.getAlias(),
                account.getBalance()
        );
    }
}
