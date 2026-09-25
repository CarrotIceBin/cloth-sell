package com.clothsell.framework.security.core.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityFrameworkUtils {
    private SecurityFrameworkUtils() {
    }

    public static Long getLoginUserId() {
        LoginUser user = current();
        return user == null || "ADMIN".equals(user.role()) ? null : user.id();
    }

    public static Long getAuthId() {
        LoginUser user = current();
        return user == null || !"ADMIN".equals(user.role()) ? null : user.id();
    }

    public static String getUserName() {
        LoginUser user = current();
        return user == null ? "" : user.name();
    }

    private static LoginUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) {
                return user;
            }
            return null;
        }
        if (authentication.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        return null;
    }

    public record LoginUser(Long id, String role, String name) {
    }
}
