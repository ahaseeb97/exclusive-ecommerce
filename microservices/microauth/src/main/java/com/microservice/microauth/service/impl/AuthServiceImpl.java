/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microauth.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.microservice.microauth.entity.Role;
import com.microservice.microauth.entity.User;
import com.microservice.microauth.repository.RoleRepository;
import com.microservice.microauth.repository.UserRepository;
import com.microservice.microauth.request.LoginRequest;
import com.microservice.microauth.request.ProfileUpdateRequest;
import com.microservice.microauth.request.RegisterRequest;
import com.microservice.microauth.request.ResetPasswordRequest;
import com.microservice.microauth.response.AuthResponse;
import com.microservice.microauth.response.TokenResponse;
import com.microservice.microauth.response.ValidationResponse;
import com.microservice.microauth.service.AuthService;
import com.microservice.microauth.service.RefreshTokenService;
import com.microservice.microauth.utils.GoogleTokenVerifierUtil;
import com.microservice.microauth.utils.JwtUtil;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author abdul.haseeb
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GoogleTokenVerifierUtil googleTokenVerifier;
    
    @Autowired
    private com.microservice.microauth.service.TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
    public User userRegister(RegisterRequest request) {
        User newuser = new User();
        newuser.setEmail(request.getEmail());
        newuser.setUsername(request.getUsername());
        newuser.setPassword(passwordEncoder.encode(request.getPassword()));
        newuser.setCreatedAt(LocalDateTime.now());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                roles.add(role);
            }
            newuser.setRoles(roles);
        }
        userRepository.save(newuser);
        return newuser;
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(","));

            // Generate access token
            String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), roles, user.getFirstName());
            
            // Generate JWT refresh token
            String refreshToken = refreshTokenService.generateRefreshToken(user.getUsername(), user.getId());
            
            return new AuthResponse("User has successfully logged in", accessToken, refreshToken);
        } else {
            throw new RuntimeException("Invalid Credentials");
        }
    }

    @Override
    public ValidationResponse validateToken(String token) {
        // First check if token is blacklisted
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            throw new RuntimeException("Token has been invalidated (logged out)");
        }
        
        // Then validate token structure and expiration
        if (jwtUtil.validToken(token)) {
            ValidationResponse response = new ValidationResponse(jwtUtil.extractUsername(token),
                    jwtUtil.extractRoles(token));

            return response;
        } else {
            throw new RuntimeException("Invalid token");
        }
    }
    
    @Override
    public AuthResponse googleLogin(String token) {

        // Verify Google token
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(token);

        if (payload == null) {
            throw new RuntimeException("Invalid Google token");
        }
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");
        String[] nameParts = name.trim().split("\\s+");

        // Find or create user
        User user = userRepository.findByEmail(email).orElse(null);
        
        System.out.println("Google Id: "+googleId);
        System.out.println("Google email: "+email);
        System.out.println("Google name: "+name);
        System.out.println("Google username: "+email.substring(0, email.indexOf("@")));
        System.out.println(payload);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setPassword("Google_Login");
            user.setUsername(email.substring(0, email.indexOf("@")));
            user.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
            user.setProvider("google");
            user.setProviderId(googleId);
            user.setLastLogin(new Date());
            user.setCreatedAt(LocalDateTime.now());

            // Assign default role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.getRoles().add(userRole);

            userRepository.save(user);
        } else {
            if (user.getProvider() == null || !user.getProvider().equals("google")) {
                user.setProvider("google");
                user.setProviderId(googleId);
            }
            user.setLastLogin(new Date());
            System.out.println(new Date());
            userRepository.save(user);
        }

        // Generate access token
        String roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), roles, user.getFirstName());
        
        // Generate JWT refresh token
        String refreshToken = refreshTokenService.generateRefreshToken(user.getUsername(), user.getId());

        return new AuthResponse("User has successfully logged in", accessToken, refreshToken);

    }
    
    @Override
    public User updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update fields only if they are provided (partial update)
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            // Check if phone is already taken by another user
            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("Phone number already in use");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        
        // Set created_at if it's null (for existing users)
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        
        return userRepository.save(user);
    }
    
    @Override
    public String resetPassword(Long userId, ResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user has a password
        if (user.getPassword() == null) {
            throw new RuntimeException("Cannot reset password for OAuth users");
        }
        
        // Check if password is "Google_Login" (for Google OAuth users setting password for first time)
        boolean isGoogleLoginPassword = "Google_Login".equals(user.getPassword());
        
        if (!isGoogleLoginPassword) {
            // For regular users, verify current password is provided and correct
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
                throw new RuntimeException("Current password is required");
            }
            
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            
            // Validate new password is different from current password
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new RuntimeException("New password must be different from current password");
            }
        }
        // For Google_Login users, skip current password verification
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        return "Password reset successfully";
    }
    
    @Override
    public String logout(String token, String refreshToken) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token is required for logout");
        }
        
        // Validate token first to ensure it's a valid token before blacklisting
        if (!jwtUtil.validToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        
        // Extract expiration time from token
        Long expirationTime = jwtUtil.extractExpiration(token);
        
        if (expirationTime == null) {
            throw new RuntimeException("Unable to extract token expiration");
        }
        
        // Add access token to blacklist until its expiration time
        tokenBlacklistService.blacklistToken(token, expirationTime);
        
        // Revoke refresh token if provided
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                refreshTokenService.revokeRefreshToken(refreshToken);
            } catch (Exception e) {
                // Log but don't fail logout if refresh token revocation fails
                System.err.println("Failed to revoke refresh token: " + e.getMessage());
            }
        }
        
        return "User has successfully logged out. Tokens have been invalidated.";
    }
    
    @Override
    public TokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Refresh token is required");
        }
        
        // Validate refresh token (checks blacklist, JWT validity, and type)
        User user = refreshTokenService.validateRefreshToken(refreshToken);
        
        // Generate new access token
        String roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));
        
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getId(), roles, user.getFirstName());
        
        // Rotate refresh token (generate new one and blacklist old one for security)
        refreshTokenService.revokeRefreshToken(refreshToken);
        String newRefreshToken = refreshTokenService.generateRefreshToken(user.getUsername(), user.getId());
        
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

}
