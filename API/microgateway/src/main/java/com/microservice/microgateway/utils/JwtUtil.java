/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microgateway.utils;

import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

/**
 *
 * @author abdul.haseeb
 */
@Component
public class JwtUtil {
    
    private static final String secretKey= "mAuthKey123456789012345678901234567890";
    SecretKey SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes());
    
    public Claims extractClaims(String token){      
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();     
    }
    
    public String extractRoles(String token){
        return extractClaims(token).get("role",String.class);
    }
    
    public boolean validToken(String token){
        try{
            extractClaims(token);
            return true;
        }
        catch(Exception ex){
            return false;
        }
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
