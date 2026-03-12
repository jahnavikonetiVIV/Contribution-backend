package com.jahnavi.contribution.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserSessionUtil {

    private UserSessionUtil() {
    }

    public static String getUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception ignored) {
        }
        return "demo.user@local";
    }
}
