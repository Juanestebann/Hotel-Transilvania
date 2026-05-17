package com.example.ms_auth_usuarios_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // GENERAR TOKEN --> Crea el JWT
    public String generateToken(String nombre, String rol) {

        return Jwts.builder()
                .subject(nombre)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getKey())
                .compact();
    }

    // EXTRAER NOMBRE --> Obtiene el usuario del token
    public String extractNombre(String token) {
        return extractAllClaims(token).getSubject();
    }

    // EXTRAER ROL --> Obtiene el rol
    public String extractRol(String token) {
        return extractAllClaims(token).get("rol", String.class);
    }

    // VALIDAR TOKEN -- verifica expiracion
    public boolean isTokenValid(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .after(new Date());
    }

    // EXTRAER CLAIMS
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}