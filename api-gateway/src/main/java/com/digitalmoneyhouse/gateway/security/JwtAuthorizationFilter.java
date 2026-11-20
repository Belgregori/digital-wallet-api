package com.digitalmoneyhouse.gateway.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtAuthorizationFilter implements GlobalFilter, Ordered {

    private final Key key;

    public JwtAuthorizationFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isPublic(request)) {
            return chain.filter(exchange);
        }
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return forbidden(exchange, "No tenés permiso para acceder");
        }
        String token = header.substring(7).trim();
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return chain.filter(exchange);
        } catch (ExpiredJwtException exception) {
            return forbidden(exchange, "El token está vencido");
        } catch (JwtException | IllegalArgumentException exception) {
            return forbidden(exchange, "No tenés permiso para acceder");
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        if (HttpMethod.POST.equals(method) && ("/users/register".equals(path) || "/auth/login".equals(path) || "/user/logout".equals(path))) {
            return true;
        }
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/webjars/");
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        byte[] body = ("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
