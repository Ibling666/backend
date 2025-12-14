package com.usei.usei.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TokenGenerator {

    private final SecretKey secretKey;

    // ✅ Lee una clave fija desde application.properties
    public TokenGenerator(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("jwt.secret no está configurado en application.properties");
        }

        // HS256 requiere al menos 32 bytes
        byte[] keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres (HS256).");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String id, String type, String username, int minutes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("type", type);
        claims.put("username", username);

        long expirationTime = (long) minutes * 60 * 1000;

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateAndParseToken(String tokenOrHeader) {
        if (tokenOrHeader == null) return null;

        String token = tokenOrHeader.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        if (token.isEmpty()) return null;

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException exception) {
            System.err.println("Token inválido: " + exception.getMessage());
            return null;
        }
    }
}
