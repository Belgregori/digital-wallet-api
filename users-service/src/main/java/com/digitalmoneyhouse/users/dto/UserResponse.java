package com.digitalmoneyhouse.users.dto;

import com.digitalmoneyhouse.users.entity.User;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String dni,
        String email,
        String phoneNumber,
        AccountResponse account
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getPhoneNumber(),
                AccountResponse.from(user.getAccount())
        );
    }
}
