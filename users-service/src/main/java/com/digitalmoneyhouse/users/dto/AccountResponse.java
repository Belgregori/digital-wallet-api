package com.digitalmoneyhouse.users.dto;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String cvu,
        String alias,
        BigDecimal balance
) {
}
