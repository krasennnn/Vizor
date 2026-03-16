package com.krasen.vizor.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expMs;

    public JwtService(
            @Value("${jwt.secret:your-256-bit-secret-key-change-this-in-production-minimum-32-characters}") String secret,
            @Value("${jwt.expirationMinutes:1440}") long minutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expMs = minutes * 60_000;
    }

    public String generate(Map<String, Object> claims, String subject) {
        var now = new Date();
        var exp = new Date(Instant.now().toEpochMilli() + expMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}


