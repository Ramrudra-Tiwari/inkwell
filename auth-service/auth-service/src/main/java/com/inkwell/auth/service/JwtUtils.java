package com.inkwell.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // ✅ Strong secret (minimum 256-bit)
    private static final String SECRET =
            "ASDFGHJKJHGFD2345678IUYTRESDFGHNMNHG3456YUYTRESDFBNBVFDWERTYUHGFV";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final long EXPIRATION = 86400000; // 1 day

    // ✅ Generate Token
    public String generateToken(String email, String role) {
        try {
            return Jwts.builder()
                    .setSubject(email)
                    .claim("role", role)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    // ✅ Extract Email
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ Extract Role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ✅ Validate Token
    public boolean validateToken(String token) {
        try {
            return extractAllClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Safe Claims Extraction
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT expired", e);
        } catch (UnsupportedJwtException e) {
            throw new RuntimeException("Unsupported JWT", e);
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Malformed JWT", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid signature", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("JWT token is empty", e);
        }
    }
}