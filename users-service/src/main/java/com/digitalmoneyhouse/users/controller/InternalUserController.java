package com.digitalmoneyhouse.users.controller;

import com.digitalmoneyhouse.users.dto.UserCredentialsResponse;
import com.digitalmoneyhouse.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping
    public UserCredentialsResponse findByEmail(@RequestParam String email) {
        return userService.findCredentialsByEmail(email);
    }
}
