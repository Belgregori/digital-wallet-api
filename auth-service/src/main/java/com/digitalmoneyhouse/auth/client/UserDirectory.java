package com.digitalmoneyhouse.auth.client;

import com.digitalmoneyhouse.auth.dto.UserCredentialsResponse;

public interface UserDirectory {

    UserCredentialsResponse findByEmail(String email);
}
