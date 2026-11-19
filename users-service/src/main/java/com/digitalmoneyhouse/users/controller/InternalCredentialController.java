package com.digitalmoneyhouse.users.controller;

import com.digitalmoneyhouse.users.dto.CredentialVerificationRequest;
import com.digitalmoneyhouse.users.dto.VerifiedUserResponse;
import com.digitalmoneyhouse.users.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/credentials")
@RequiredArgsConstructor
public class InternalCredentialController {

    private final UserService userService;

    @PostMapping("/verify")
    public VerifiedUserResponse verify(@Valid @RequestBody CredentialVerificationRequest request) {
        String email = userService.verifyCredentials(request.email(), request.password());
        return new VerifiedUserResponse(email);
    }
}
