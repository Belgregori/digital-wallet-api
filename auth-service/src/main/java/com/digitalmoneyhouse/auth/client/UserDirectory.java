package com.digitalmoneyhouse.auth.client;

public interface UserDirectory {

    String authenticate(String email, String password);
}
