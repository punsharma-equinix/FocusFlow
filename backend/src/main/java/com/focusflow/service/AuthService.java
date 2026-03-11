package com.focusflow.service;

import com.focusflow.dto.Dto;
import com.focusflow.entity.User;
import com.focusflow.repository.UserRepository;
import com.focusflow.security.JwtService;
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

    public Dto.AuthResponse register(Dto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
            .name(req.getName())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .plan(User.Plan.FREE)
            .build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return new Dto.AuthResponse(token, Dto.UserResponse.from(user));
    }

    public Dto.AuthResponse login(Dto.LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtService.generateToken(user.getEmail());
        return new Dto.AuthResponse(token, Dto.UserResponse.from(user));
    }
}
