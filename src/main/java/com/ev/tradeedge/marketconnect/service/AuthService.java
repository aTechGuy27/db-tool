package com.ev.tradeedge.marketconnect.service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ev.tradeedge.marketconnect.model.auth.AuthRequest;
import com.ev.tradeedge.marketconnect.model.auth.AuthResponse;
import com.ev.tradeedge.marketconnect.model.auth.RegisterRequest;
import com.ev.tradeedge.marketconnect.repository.UserRepository;
import com.ev.tradeedge.marketconnect.security.JwtTokenProvider;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest loginRequest) {
        try {
            // Check if user exists
            Optional<Map<String, Object>> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            
            if (userOpt.isEmpty()) {
                return new AuthResponse(false, null, "Invalid username or password");
            }
            
            Map<String, Object> user = userOpt.get();
            
            // Create authentication object
            @SuppressWarnings("unused")
			UserDetails userDetails = new User(
                loginRequest.getUsername(),
                user.get("password").toString(),
                Collections.singletonList(new SimpleGrantedAuthority(
                    (Boolean) user.get("is_admin") ? "ROLE_ADMIN" : "ROLE_USER"
                ))
            );
            
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT token
            String jwt = tokenProvider.generateToken(authentication);
            
            // Update last login time
            userRepository.updateLastLogin(loginRequest.getUsername());
            
            return new AuthResponse(true, jwt, "Login successful");
        } catch (Exception e) {
            return new AuthResponse(false, null, "Authentication failed: " + e.getMessage());
        }
    }

    public Long register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        
        // Save user
        return userRepository.save(
            registerRequest.getUsername(),
            encodedPassword,
            registerRequest.getFullName(),
            registerRequest.isAdmin()
        );
    }
}