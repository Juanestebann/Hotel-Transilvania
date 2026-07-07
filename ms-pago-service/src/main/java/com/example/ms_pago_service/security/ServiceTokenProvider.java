package com.example.ms_pago_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class ServiceTokenProvider {

    private static final String SERVICE_NAME = "ms-pago-service";
    private static final Duration TOKEN_DURATION = Duration.ofMinutes(2);

    private final SecretKey key;

    public ServiceTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getAuthorizationHeader() {
        Instant now = Instant.now();

        String token = Jwts.builder()
                .subject(SERVICE_NAME)
                .claim("rol", "SERVICE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_DURATION)))
                .signWith(key)
                .compact();

        return "Bearer " + token;
    }
}
