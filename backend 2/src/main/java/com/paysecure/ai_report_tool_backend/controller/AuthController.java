package com.paysecure.ai_report_tool_backend.controller;

import com.paysecure.ai_report_tool_backend.dto.auth.AuthResponse;
import com.paysecure.ai_report_tool_backend.dto.auth.LoginRequest;
import com.paysecure.ai_report_tool_backend.dto.auth.RegisterRequest;
import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.security.SecurityUtils;
import com.paysecure.ai_report_tool_backend.service.AuthService;
import com.paysecure.ai_report_tool_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public AuthResponse me() {
        UUID userId = SecurityUtils.getCurrentUserId();
//        .info("AUTH = {}", SecurityContextHolder.getContext().getAuthentication());
        User user = userService.getById(userId);
        return AuthResponse.from(user);
    }
}