package com.paysecure.ai_report_tool_backend.dto.auth;


import com.paysecure.ai_report_tool_backend.model.User;
import com.paysecure.ai_report_tool_backend.model.enums.Plan;
import com.paysecure.ai_report_tool_backend.model.enums.Role;


import java.util.UUID;

public record AuthResponse(
        UUID id,
        String email,
        String full_name,
        Role role,
        int credits,
        Plan plan,
        String token
) {

    public static AuthResponse from(User user, String token) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCredits(),
                user.getPlan(),
                token
        );
    }

    // For /me (token already known)
    public static AuthResponse from(User user) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCredits(),
                user.getPlan(),
                null
        );
    }
}