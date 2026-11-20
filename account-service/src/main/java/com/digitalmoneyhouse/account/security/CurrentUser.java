package com.digitalmoneyhouse.account.security;

import com.digitalmoneyhouse.account.exception.ForbiddenException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUser {

    public String email() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new ForbiddenException("No tenés permiso para acceder");
        }
        Object email = servletAttributes.getRequest().getAttribute(AccessTokenFilter.EMAIL_ATTRIBUTE);
        if (!(email instanceof String value) || value.isBlank()) {
            throw new ForbiddenException("No tenés permiso para acceder");
        }
        return value;
    }
}
