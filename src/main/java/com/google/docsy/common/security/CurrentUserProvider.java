package com.google.docsy.common.security;

import com.google.docsy.common.exception.UnauthorizedException;
import com.google.docsy.feature.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    /**
     * Retrieves the fully populated User entity of the currently authenticated user.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("User is not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser();
    }

    /**
     * Convenience method if you only need the ID and don't want to load the whole object
     */
    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}