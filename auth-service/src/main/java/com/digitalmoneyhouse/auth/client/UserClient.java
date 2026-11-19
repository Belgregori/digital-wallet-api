package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.dto.CredentialVerificationRequest;
import com.digitalmoneyhouse.auth.dto.VerifiedUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "users-service")
public interface UserClient {

    @PostMapping("/internal/credentials/verify")
    VerifiedUserResponse verify(@RequestBody CredentialVerificationRequest request);
}
