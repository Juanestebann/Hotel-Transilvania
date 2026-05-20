package com.example.ms_disponibilidad_service.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

    private final HttpServletRequest request;

    public TokenProvider(HttpServletRequest request) {
        this.request = request;
    }

    public String getAuthorizationHeader() {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
