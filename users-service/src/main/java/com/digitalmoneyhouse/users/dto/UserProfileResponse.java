package com.digitalmoneyhouse.users.dto;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String email,
        String phoneNumber,
        String cvu,
        String alias
) {
}
