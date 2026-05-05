/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microgateway.utils;

import com.microservice.microgateway.service.TokenBlacklistService;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * @author abdul.haseeb
 */
@Component
public class AuthFilter implements GatewayFilter {

    private JwtUtil jwtUtil;
    
    private TokenBlacklistService tokenBlacklistService;

    private String requiredRole;

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    public void setTokenBlacklistService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        System.out.println("=== AUTH FILTER DEBUG ===");
        System.out.println("Path: " + request.getPath());
        System.out.println("Required Role: " + requiredRole);

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {

            System.out.println("NO AUTHORIZATION HEADER");
            return this.onError(exchange, "Missing authorization Header", HttpStatus.UNAUTHORIZED);

        }

        String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
        // Normalize token - make it final for use in lambda
        final String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            token = authHeader != null ? authHeader.trim() : "";
        }

        // First check if token is blacklisted (logged out)
        return tokenBlacklistService.isTokenBlacklisted(token)
            .flatMap(isBlacklisted -> {
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    System.out.println("TOKEN IS BLACKLISTED (LOGGED OUT)");
                    return this.onError(exchange, "Token has been invalidated (logged out)", HttpStatus.UNAUTHORIZED);
                }
                
                // Then validate token structure and expiration
                try {
                    if (!jwtUtil.validToken(token)) {
                        System.out.println("INVALID TOKEN");
                        return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    }
                    
                    // Reject refresh tokens - they should only be used in /auth/refresh endpoint
                    if (jwtUtil.isRefreshToken(token)) {
                        System.out.println("REFRESH TOKEN USED IN API REQUEST");
                        return this.onError(exchange, "Refresh tokens cannot be used for API access. Please use access token.", HttpStatus.UNAUTHORIZED);
                    }

                    String userRole = jwtUtil.extractRoles(token);

                    List<String> userRoles = Arrays.asList(userRole.split(","));
                    
                    boolean hasAccess = Arrays.stream(requiredRole.split(","))
                            .map(String::trim)
                            .anyMatch(userRoles::contains);

                    if (!hasAccess) {
                        System.out.println("UNAUTHORIZED ACCESS");
                        return this.onError(exchange, "Unauthorized Access", HttpStatus.FORBIDDEN);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR");
                    return this.onError(exchange, "Authorization Error", HttpStatus.UNAUTHORIZED);
                }
                
                return chain.filter(exchange);
            })
            .onErrorResume(e -> {
                System.out.println("ERROR CHECKING BLACKLIST: " + e.getMessage());
                // If Redis check fails, continue with normal validation
                // This ensures service availability even if Redis is down
                try {
                    if (!jwtUtil.validToken(token)) {
                        return this.onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    }
                    
                    // Reject refresh tokens even in fallback mode
                    if (jwtUtil.isRefreshToken(token)) {
                        return this.onError(exchange, "Refresh tokens cannot be used for API access. Please use access token.", HttpStatus.UNAUTHORIZED);
                    }

                    String userRole = jwtUtil.extractRoles(token);
                    List<String> userRoles = Arrays.asList(userRole.split(","));
                    
                    boolean hasAccess = Arrays.stream(requiredRole.split(","))
                            .map(String::trim)
                            .anyMatch(userRoles::contains);

                    if (!hasAccess) {
                        return this.onError(exchange, "Unauthorized Access", HttpStatus.FORBIDDEN);
                    }
                    
                    return chain.filter(exchange);
                } catch (Exception ex) {
                    return this.onError(exchange, "Authorization Error", HttpStatus.UNAUTHORIZED);
                }
            });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String errorMessage = "{\"error\": \"" + error + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

}
