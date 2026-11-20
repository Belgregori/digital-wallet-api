package com.digitalmoneyhouse.account.dto;

import com.digitalmoneyhouse.account.entity.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardRequest(
        Long cardId,

        @Pattern(regexp = "\\d{13,19}", message = "El número de tarjeta debe tener entre 13 y 19 dígitos")
        String number,

        @Size(min = 2, max = 50, message = "El titular debe tener entre 2 y 50 caracteres")
        String holderName,

        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "El vencimiento debe tener formato MM/yy")
        String expirationDate,

        @Pattern(regexp = "\\d{3,4}", message = "El CVV debe tener 3 o 4 dígitos")
        String cvv,

        CardType type
) {

    public void requireCardData() {
        if (number == null || holderName == null || expirationDate == null || cvv == null || type == null) {
            throw new com.digitalmoneyhouse.account.exception.ValidationException(
                    "Faltan datos de la tarjeta");
        }
    }
}
