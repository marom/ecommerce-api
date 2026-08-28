package com.marom.ecommerce.api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.exception.AccessDeniedException;

/**
 * Reads the authenticated principal minted by {@link JwtService} and validated by the
 * resource server. Controllers use this instead of trusting ids in request bodies.
 */
@Component
public class CurrentUserService {

    public long currentUserId() {
        return numberClaim("uid").longValue();
    }

    public String currentEmail() {
        return jwt().getSubject();
    }

    public boolean isAdmin() {
        return authentication().getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()));
    }

    /**
     * The customer this caller acts as. Admin tokens carry no {@code customerId} claim,
     * so admins cannot place orders or post reviews — they get a 403.
     */
    public Long currentCustomerId() {
        Object claim = jwt().getClaim("customerId");
        if (claim == null) {
            throw new AccessDeniedException("This action requires a customer account");
        }
        return ((Number) claim).longValue();
    }

    private Number numberClaim(String name) {
        Object claim = jwt().getClaim(name);
        if (claim == null) {
            throw new AccessDeniedException("Malformed authentication token: missing '" + name + "'");
        }
        return (Number) claim;
    }

    private Jwt jwt() {
        Object principal = authentication().getPrincipal();
        if (principal instanceof Jwt token) {
            return token;
        }
        throw new AccessDeniedException("No authenticated user");
    }

    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }
        return authentication;
    }
}
