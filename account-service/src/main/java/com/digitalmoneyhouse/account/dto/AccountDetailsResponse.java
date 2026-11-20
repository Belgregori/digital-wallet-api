package com.digitalmoneyhouse.account.dto;

import com.digitalmoneyhouse.account.entity.Account;

import java.math.BigDecimal;

public record AccountDetailsResponse(
        Long id,
        Long userId,
        String cvu,
        String alias,
        BigDecimal balance
) {

    public static AccountDetailsResponse from(Account account) {
        return new AccountDetailsResponse(
                account.getId(),
                account.getUserId(),
                account.getCvu(),
                account.getAlias(),
                account.getBalance()
        );
    }
}
