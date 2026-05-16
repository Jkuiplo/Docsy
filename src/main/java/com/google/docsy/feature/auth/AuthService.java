package com.google.docsy.feature.auth;

import com.google.docsy.common.exception.ConflictException;
import com.google.docsy.common.security.JwtService;
import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.auth.dto.request.LoginRequest;
import com.google.docsy.feature.auth.dto.request.RegisterRequest;
import com.google.docsy.feature.auth.dto.response.AuthResponse;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.user.UserRepository;
import com.google.docsy.feature.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        System.out.println("REGISTER REQUEST:");
        System.out.println("email = " + request.getEmail());
        System.out.println("password = " + request.getPassword());
        System.out.println("fullName = " + request.getFullName());
        System.out.println("positionTitle = " + request.getPositionTitle());

        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("EMAIL ALREADY EXISTS: " + request.getEmail());
            throw new ConflictException("Email is already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPositionTitle(request.getPositionTitle());
        user.setEmailVerified(false);

        System.out.println("SAVING USER...");
        userRepository.save(user);
        System.out.println("USER SAVED: " + user.getId());

        System.out.println("GENERATING JWT...");
        UserPrincipal principal = new UserPrincipal(user);
        String jwtToken = jwtService.generateToken(principal);
        System.out.println("JWT GENERATED");

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
                
        UserPrincipal principal = new UserPrincipal(user);
        String jwtToken = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(jwtToken)
                .user(mapToUserResponse(user))
                .build();
    }
    
    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .positionTitle(user.getPositionTitle())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}