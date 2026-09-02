package com.clinc_cms.management.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret:change-this-to-a-long-random-secret-in-properties}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String subjectEmailOrUsername, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .setSubject(subjectEmailOrUsername)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(String token, String roleRequired) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

            if (roleRequired != null && !roleRequired.equalsIgnoreCase(String.valueOf(claims.get("role")))) {
                return false;
            }
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public String extractRole(String token) {
        return (String) Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("role");
    }
}
