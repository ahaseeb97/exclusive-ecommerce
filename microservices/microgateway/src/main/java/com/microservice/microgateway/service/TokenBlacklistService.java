/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microgateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service to check if JWT tokens are blacklisted in Redis
 * Uses reactive Redis for Spring WebFlux compatibility
 * 
 * @author abdul.haseeb
 */
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /**
     * Check if a token is blacklisted
     * @param token The JWT token to check
     * @return Mono<Boolean> - true if token is blacklisted, false otherwise
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return Mono.just(false);
        }
        
        String normalizedToken = normalizeToken(token);
        String redisKey = BLACKLIST_PREFIX + normalizedToken;
        
        // Check if key exists in Redis
        // Returns Mono<Boolean> - true if key exists (token is blacklisted)
        return reactiveRedisTemplate.hasKey(redisKey)
            .defaultIfEmpty(false);
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
}

