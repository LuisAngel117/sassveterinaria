package com.sassveterinaria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(
        UUID userId,
        String username,
        String fullName,
        String roleCode,
        UUID branchId,
        List<String> permissions
    ) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.accessTokenSeconds());

        Map<String, Object> claims = Map.of(
            "role", roleCode,
            "branch_id", branchId.toString(),
            "perms", permissions,
            "username", username,
            "full_name", fullName
        );

        return Jwts.builder()
            .claims(claims)
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is required");
        }

        byte[] keyBytes;
        if (isBase64(secret)) {
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isBase64(String value) {
        try {
            Decoders.BASE64.decode(value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
