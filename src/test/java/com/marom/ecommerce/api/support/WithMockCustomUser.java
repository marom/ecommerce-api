package com.marom.ecommerce.api.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Populates the {@link org.springframework.security.core.context.SecurityContext} with a
 * {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken}
 * shaped exactly like the one the resource server produces from a real token — same
 * {@code sub} / {@code roles} / {@code uid} / {@code customerId} claims and {@code ROLE_*}
 * authority — so {@code CurrentUserService} behaves as in production.
 *
 * <p>A {@code ROLE_ADMIN} role omits the {@code customerId} claim, mirroring admin tokens.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    long userId() default 1L;

    long customerId() default 1L;

    String role() default "ROLE_CUSTOMER";

    String email() default "user@example.com";
}
