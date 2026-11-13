package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.dto.UserCredentialsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "users-service")
public interface UserClient {

    @GetMapping("/internal/users")
    UserCredentialsResponse findByEmail(@RequestParam("email") String email);
}
