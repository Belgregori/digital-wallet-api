package com.digitalmoneyhouse.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AliasUpdateRequest(
        @NotBlank(message = "El alias es obligatorio")
        @Pattern(regexp = "^[a-z]+\\.[a-z]+\\.[a-z]+$", message = "El alias debe ser tres palabras separadas por puntos")
        String alias
) {
}
