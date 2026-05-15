/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microauth.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service to manage blacklisted JWT tokens using Redis
 * Tokens are stored in Redis with TTL (Time To Live) for automatic expiration
 * Redis handles cleanup automatically, making this solution scalable and persistent
 * 
 * @author abdul.haseeb
 */
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Add a token to the blacklist
     * @param token The JWT token to blacklist
     * @param expirationTime The expiration time of the token (in milliseconds since epoch)
     */
    public void blacklistToken(String token, long expirationTime) {
        if (token != null && !token.isEmpty()) {
            String normalizedToken = normalizeToken(token);
            String redisKey = BLACKLIST_PREFIX + normalizedToken;
            
            // Calculate TTL in seconds
            long currentTime = System.currentTimeMillis();
            long ttlSeconds = (expirationTime - currentTime) / 1000;
            
            // Only add to blacklist if token hasn't expired yet
            if (ttlSeconds > 0) {
                // Store token with value "1" and set TTL
                // Redis will automatically remove the key when TTL expires
                redisTemplate.opsForValue().set(redisKey, "1", ttlSeconds, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Check if a token is blacklisted
     * @param token The JWT token to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        String normalizedToken = normalizeToken(token);
        String redisKey = BLACKLIST_PREFIX + normalizedToken;
        
        // Check if key exists in Redis
        // If key doesn't exist, token is not blacklisted
        // Redis automatically removes expired keys, so we don't need manual cleanup
        Boolean exists = redisTemplate.hasKey(redisKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Remove a token from the blacklist (if it exists)
     * @param token The JWT token to remove from blacklist
     */
    public void removeFromBlacklist(String token) {
        if (token != null && !token.isEmpty()) {
            String normalizedToken = normalizeToken(token);
            String redisKey = BLACKLIST_PREFIX + normalizedToken;
            redisTemplate.delete(redisKey);
        }
    }

    /**
     * Normalize token by removing "Bearer " prefix if present
     * @param token The token to normalize
     * @return Normalized token
     */
    private String normalizeToken(String token) {
        if (token != null && token.toLowerCase().startsWith("bearer ")) {
            return token.substring(7).trim();
        }
        return token != null ? token.trim() : "";
    }

    /**
     * Get the number of blacklisted tokens (for monitoring/debugging)
     * Note: This operation can be expensive on large Redis instances
     * @return The count of blacklisted tokens
     */
    public int getBlacklistSize() {
        try {
            // Count keys matching the blacklist prefix pattern
            // This is a simple implementation - for production, consider using SCAN for better performance
            return redisTemplate.keys(BLACKLIST_PREFIX + "*").size();
        } catch (Exception e) {
            // If Redis is unavailable or error occurs, return 0
            return 0;
        }
    }
}

