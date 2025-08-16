package com.utem.event_hub_navigation.utils;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtTokenUtil {

    
    private static String SECRET_KEY;

    
    private static long JWT_EXPIRATION_MS;

    @Value("${jwt.secretKey}")
    public void setSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
    }

    @Value("${jwt.expirationMs}")
    public void setJwtExpirationMs(long jwtExpirationMs) {
        JWT_EXPIRATION_MS = jwtExpirationMs;
    }

    public static String generateToken(String email, String role) {
        // Header.Payload.Signature
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.builder()
                .subject(email)
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .issuedAt(new Date())
                .claim("role", role)
                .signWith(key)
                .compact();

    }

    public static boolean validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        System.out.println("validateToken");
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;

        }
    }

    public static String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build().parseSignedClaims(token).getPayload();

        return claims.getSubject();
    }

    public static String getRoleFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }
    
}
