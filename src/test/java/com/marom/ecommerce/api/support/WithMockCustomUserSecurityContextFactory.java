package com.marom.ecommerce.api.support;

import java.time.Instant;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        Instant now = Instant.now();
        Jwt.Builder jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(annotation.email())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("roles", List.of(annotation.role()))
                .claim("uid", annotation.userId());

        if (!"ROLE_ADMIN".equals(annotation.role())) {
            jwt.claim("customerId", annotation.customerId());
        }

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt.build(),
                List.of(new SimpleGrantedAuthority(annotation.role())),
                annotation.email());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
