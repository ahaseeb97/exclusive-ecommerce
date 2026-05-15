/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microservice.microauth.service;

import com.microservice.microauth.entity.User;
import com.microservice.microauth.request.LoginRequest;
import com.microservice.microauth.request.ProfileUpdateRequest;
import com.microservice.microauth.request.RegisterRequest;
import com.microservice.microauth.request.ResetPasswordRequest;
import com.microservice.microauth.response.AuthResponse;
import com.microservice.microauth.response.TokenResponse;
import com.microservice.microauth.response.ValidationResponse;

/**
 *
 * @author abdul.haseeb
 */
public interface AuthService {
    
    User userRegister(RegisterRequest registerRequest);
    
    AuthResponse loginUser (LoginRequest loginRequest);
    
    ValidationResponse validateToken (String token);
    
    AuthResponse googleLogin(String token);
    
    User updateProfile(Long userId, ProfileUpdateRequest request);
    
    String resetPassword(Long userId, ResetPasswordRequest request);
    
    String logout(String token, String refreshToken);
    
    TokenResponse refreshToken(String refreshToken);
    
}
