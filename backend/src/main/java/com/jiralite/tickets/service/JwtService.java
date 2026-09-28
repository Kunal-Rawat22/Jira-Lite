package com.jiralite.tickets.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlMs;

    public JwtService(
            @Value("${jwt.secret}") String secret, @Value("${jwt.ttl-ms}") long ttlMs) {
        this.key = Keys.hmacShaKeyFor(pad(secret).getBytes(StandardCharsets.UTF_8));
        this.ttlMs = ttlMs;
    }

    public String issue(UUID userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long ttlMs() {
        return ttlMs;
    }

    private static String pad(String secret) {
        if (secret.length() >= 32) {
            return secret;
        }
        return (secret + "0123456789abcdef0123456789abcdef").substring(0, 32);
    }
}
