package com.digitalmoneyhouse.auth.dto;

public record ErrorResponse(int status, String error, String message) {
}
