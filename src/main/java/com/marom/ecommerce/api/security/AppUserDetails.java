package com.marom.ecommerce.api.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.marom.ecommerce.api.entity.User;

import lombok.Getter;

/**
 * Immutable {@link UserDetails} view of a {@link User}, used only during the
 * username/password authentication that {@code AuthService.login} performs.
 */
@Getter
public class AppUserDetails implements UserDetails {

    private final long userId;
    private final Long customerId;
    private final String username;
    private final String password;
    private final String role;
    private final boolean enabled;

    public AppUserDetails(User user) {
        this.userId = user.getId();
        this.customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;
        this.username = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole().name();
        this.enabled = user.isEnabled();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
