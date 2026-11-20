package com.digitalmoneyhouse.account.dto;

import com.digitalmoneyhouse.account.entity.Card;
import com.digitalmoneyhouse.account.entity.CardType;

public record CardResponse(
        Long id,
        Long accountId,
        String number,
        String holderName,
        String expirationDate,
        CardType type
) {

    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getAccountId(),
                card.getNumber(),
                card.getHolderName(),
                card.getExpirationDate(),
                card.getType()
        );
    }
}
