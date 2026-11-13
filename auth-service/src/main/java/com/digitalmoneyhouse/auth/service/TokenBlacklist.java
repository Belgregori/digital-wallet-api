package com.digitalmoneyhouse.auth.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Set<String> tokens = ConcurrentHashMap.newKeySet();

    public void invalidate(String token) {
        tokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return tokens.contains(token);
    }
}
