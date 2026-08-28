package com.marom.ecommerce.api.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.marom.ecommerce.api.exception.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate(String role, Map<String, Object> extraClaims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("roles", List.of(role));
        extraClaims.forEach(builder::claim);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(builder.build(),
                List.of(new SimpleGrantedAuthority(role)), "user@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void should_returnCustomerId_when_customerTokenHasClaim() {
        // Arrange
        authenticate("ROLE_CUSTOMER", Map.of("uid", 2L, "customerId", 7L));

        // Act
        Long customerId = currentUserService.currentCustomerId();

        // Assert
        assertThat(customerId).isEqualTo(7L);
    }

    @Test
    void should_throwAccessDenied_when_adminTokenHasNoCustomerId() {
        // Arrange
        authenticate("ROLE_ADMIN", Map.of("uid", 1L));

        // Act & Assert
        assertThatThrownBy(currentUserService::currentCustomerId)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("customer account");
    }

    @Test
    void should_reportIsAdmin_when_tokenHasAdminAuthority() {
        // Arrange
        authenticate("ROLE_ADMIN", Map.of("uid", 1L));

        // Act & Assert
        assertThat(currentUserService.isAdmin()).isTrue();
        assertThat(currentUserService.currentEmail()).isEqualTo("user@example.com");
    }

    @Test
    void should_notReportIsAdmin_when_tokenHasCustomerAuthority() {
        // Arrange
        authenticate("ROLE_CUSTOMER", Map.of("uid", 2L, "customerId", 7L));

        // Act & Assert
        assertThat(currentUserService.isAdmin()).isFalse();
    }
}
