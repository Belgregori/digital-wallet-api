package com.digitalmoneyhouse.users.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccessTokenFilter extends OncePerRequestFilter {

    public static final String EMAIL_ATTRIBUTE = "authenticatedEmail";

    private final JwtTokenService jwtTokenService;

    public AccessTokenFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/users/register".equals(path)
                || path.startsWith("/internal/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            writeForbidden(response, "No tenés permiso para acceder");
            return;
        }
        String token = header.substring(7).trim();
        switch (jwtTokenService.inspect(token)) {
            case EXPIRED -> {
                writeForbidden(response, "El token está vencido");
                return;
            }
            case INVALID -> {
                writeForbidden(response, "No tenés permiso para acceder");
                return;
            }
            case VALID -> request.setAttribute(EMAIL_ATTRIBUTE, jwtTokenService.extractEmail(token));
        }
        filterChain.doFilter(request, response);
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"" + message + "\"}");
    }
}
