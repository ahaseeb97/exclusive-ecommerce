/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microauth.service;

import com.microservice.microauth.entity.User;
import com.microservice.microauth.repository.UserRepository;
import com.microservice.microauth.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to manage JWT refresh tokens
 * Uses JWT tokens with Redis blacklist for revocation
 * 
 * @author abdul.haseeb
 */
@Service
public class RefreshTokenService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * Generate a JWT refresh token for a user
     * @param username Username
     * @param userId User ID
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String username, Long userId) {
        return jwtUtil.generateRefreshToken(username, userId);
    }

    /**
     * Validate a refresh token
     * Checks if token is valid JWT, is a refresh token, and is not blacklisted
     * @param token Refresh token string
     * @return User entity if valid, throws exception otherwise
     */
    public User validateRefreshToken(String token) {
        // Check if token is blacklisted (logged out)
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new RuntimeException("Refresh token has been revoked (logged out)");
        }
        
        // Validate token structure and expiration
        if (!jwtUtil.validToken(token)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // Check if token is actually a refresh token
        if (!jwtUtil.isRefreshToken(token)) {
            throw new RuntimeException("Token is not a refresh token");
        }
        
        // Extract user info and return user
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            throw new RuntimeException("Unable to extract user ID from refresh token");
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Revoke a refresh token by adding it to blacklist
     * @param token Refresh token string
     */
    public void revokeRefreshToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        // Validate token first to get expiration
        if (jwtUtil.validToken(token)) {
            Long expirationTime = jwtUtil.extractExpiration(token);
            if (expirationTime != null) {
                // Add to blacklist until expiration
                tokenBlacklistService.blacklistToken(token, expirationTime);
            }
        }
    }
}

