/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microgateway.utils;

import com.microservice.microgateway.service.TokenBlacklistService;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author abdul.haseeb
 */
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config>  {
    
    @Autowired
    JwtUtil jwtUtil;
    
    @Autowired
    TokenBlacklistService tokenBlacklistService;

    public AuthGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config){
        
        AuthFilter authFilter = new AuthFilter();
        authFilter.setJwtUtil(jwtUtil);
        authFilter.setTokenBlacklistService(tokenBlacklistService);
        authFilter.setRequiredRole(config.getRole());
        return authFilter;
        
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("role");
    }
    
    public static class Config{
        
        private String role;
        
        public String getRole(){
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
        
    }
    
}
