package com.digitalmoneyhouse.users.dto;

public record ErrorResponse(
        int status,
        String error,
        String message
) {
}
