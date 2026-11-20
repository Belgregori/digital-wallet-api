package com.digitalmoneyhouse.users.controller;

import com.digitalmoneyhouse.users.dto.UserProfileResponse;
import com.digitalmoneyhouse.users.dto.UserRegisterRequest;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.dto.UserUpdateRequest;
import com.digitalmoneyhouse.users.security.CurrentUser;
import com.digitalmoneyhouse.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/{id}")
    public UserProfileResponse profile(@PathVariable Long id) {
        return userService.profile(id, currentUser.email());
    }

    @PutMapping("/{id}")
    public UserProfileResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, currentUser.email(), request);
    }
}
