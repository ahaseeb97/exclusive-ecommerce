/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microauth.utils;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;

/**
 *
 * @author abdul.haseeb
 */
@Component
public class JwtUtil {

    private static final String secretKey = "mAuthKey123456789012345678901234567890";
    SecretKey SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());

    public String generateToken(String username, Long userId, String role, String name) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId",userId);
        claims.put("role", role);
        claims.put("name", name);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 1)) // 1 hour
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        if (token != null && token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRoles(String token) {
        return extractClaims(token).get("role", String.class);
    }
    
    public Long extractUserId(String token) {
        Object userIdObj = extractClaims(token).get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }

    public boolean validToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * Extract the expiration time from a token
     * @param token The JWT token
     * @return The expiration time in milliseconds since epoch, or null if token is invalid
     */
    public Long extractExpiration(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null ? expiration.getTime() : null;
        } catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Generate a JWT refresh token (long-lived token for getting new access tokens)
     * Refresh tokens are JWT tokens with 7 days expiration
     * @param username Username
     * @param userId User ID
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh"); // Mark as refresh token
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7 days
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Check if a token is a refresh token
     * @param token The JWT token
     * @return true if token is a refresh token, false otherwise
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractClaims(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception ex) {
            return false;
        }
    }
}
