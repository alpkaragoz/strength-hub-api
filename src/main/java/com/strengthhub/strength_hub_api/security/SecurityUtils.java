package com.strengthhub.strength_hub_api.security;

import com.strengthhub.strength_hub_api.exception.common.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SecurityUtils {

    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new UnauthorizedAccessException("No authenticated user found");
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserPrincipal().getUserId();
    }

    public static String getCurrentUsername() {
        return getCurrentUserPrincipal().getUsername();
    }

    public static boolean isCurrentUserAdmin() {
        try {
            return getCurrentUserPrincipal().getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        } catch (UnauthorizedAccessException e) {
            return false;
        }
    }

    public static boolean isCurrentUserCoach() {
        try {
            return getCurrentUserPrincipal().getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_COACH"));
        } catch (UnauthorizedAccessException e) {
            return false;
        }
    }

    public static boolean isCurrentUserLifter() {
        try {
            return getCurrentUserPrincipal().getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_LIFTER"));
        } catch (UnauthorizedAccessException e) {
            return false;
        }
    }

    public static boolean isCurrentUserOrAdmin(UUID userId) {
        try {
            UserPrincipal principal = getCurrentUserPrincipal();
            return isCurrentUserAdmin() || principal.getUserId().equals(userId);
        } catch (UnauthorizedAccessException e) {
            return false;
        }
    }

    public static void requireAdmin() {
        if (!isCurrentUserAdmin()) {
            throw new UnauthorizedAccessException("Admin privileges required");
        }
    }

    public static void requireCoach() {
        if (!isCurrentUserCoach() && !isCurrentUserAdmin()) {
            throw new UnauthorizedAccessException("Coach privileges required");
        }
    }

    public static void requireLifter() {
        if (!isCurrentUserLifter() && !isCurrentUserAdmin()) {
            throw new UnauthorizedAccessException("Lifter privileges required");
        }
    }

    public static void requireCurrentUserOrAdmin(UUID userId) {
        if (!isCurrentUserOrAdmin(userId)) {
            throw new UnauthorizedAccessException("Access denied: can only access own data");
        }
    }
}