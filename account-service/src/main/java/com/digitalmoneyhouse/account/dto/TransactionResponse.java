package com.digitalmoneyhouse.account.dto;

import com.digitalmoneyhouse.account.entity.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long accountId,
        BigDecimal amount,
        Instant date,
        String description
) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getAmount(),
                transaction.getCreatedAt(),
                transaction.getDescription()
        );
    }
}
