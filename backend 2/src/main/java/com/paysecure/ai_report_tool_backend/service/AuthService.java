package com.paysecure.ai_report_tool_backend.service;


import com.paysecure.ai_report_tool_backend.exception.ApiException;
import com.paysecure.ai_report_tool_backend.model.enums.Plan;
import com.paysecure.ai_report_tool_backend.model.enums.Role;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.dto.auth.AuthResponse;
import com.paysecure.ai_report_tool_backend.dto.auth.LoginRequest;
import com.paysecure.ai_report_tool_backend.dto.auth.RegisterRequest;
import com.paysecure.ai_report_tool_backend.repository.UserRepository;
import com.paysecure.ai_report_tool_backend.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /* -------------------------
       Register
    ------------------------- */
    public AuthResponse register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(req.email());
        user.setFullName(req.full_name());
        user.setPassword(passwordEncoder.encode(req.password()));
        // Role can be "ADMIN" or "USER"
        user.setRole(Role.USER);
        user.setCredits(1000);
        user.setPlan(Plan.Free);

        userRepository.save(user);

        String token = jwtService.generateToken(
                user.getId(),
                user.getRole().name()
        );

        return AuthResponse.from(user, token);
    }

    /* -------------------------
       Login
    ------------------------- */
    public AuthResponse login(LoginRequest req) {

        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() ->
                        new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED)
                );

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getRole().name()
        );

        return AuthResponse.from(user, token);
    }
}