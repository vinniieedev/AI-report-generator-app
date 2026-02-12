package com.paysecure.ai_report_tool_backend.security;

import com.paysecure.ai_report_tool_backend.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null ||
                "anonymousUser".equals(authentication.getPrincipal())) {

            throw new ApiException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String id) {
            try {
                return UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                throw new ApiException("Invalid user id", HttpStatus.UNAUTHORIZED);
            }
        }

        throw new ApiException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }
}