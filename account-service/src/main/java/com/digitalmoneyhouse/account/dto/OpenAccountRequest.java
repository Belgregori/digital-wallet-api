package com.digitalmoneyhouse.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OpenAccountRequest(
        @NotNull(message = "El usuario es obligatorio")
        Long userId,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String email
) {
}
