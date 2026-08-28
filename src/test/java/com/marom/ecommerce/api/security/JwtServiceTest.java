package com.marom.ecommerce.api.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        jwtService = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(key)),
                "ecommerce-api-test", 3600L);
        jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Test
    void should_mintTokenWithCustomerClaims_when_userIsACustomer() {
        // Arrange
        User user = User.builder()
                .id(2L)
                .email("john.doe@example.com")
                .role(Role.ROLE_CUSTOMER)
                .customer(Customer.builder().id(1L).build())
                .build();

        // Act
        Jwt decoded = jwtDecoder.decode(jwtService.issue(user));

        // Assert
        assertThat(decoded.getSubject()).isEqualTo("john.doe@example.com");
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("ROLE_CUSTOMER");
        assertThat(((Number) decoded.getClaim("uid")).longValue()).isEqualTo(2L);
        assertThat(((Number) decoded.getClaim("customerId")).longValue()).isEqualTo(1L);
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
    }

    @Test
    void should_mintTokenWithoutCustomerClaim_when_userIsAnAdmin() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("admin@shop.example.com")
                .role(Role.ROLE_ADMIN)
                .customer(null)
                .build();

        // Act
        Jwt decoded = jwtDecoder.decode(jwtService.issue(user));

        // Assert
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly("ROLE_ADMIN");
        assertThat(decoded.<Object>getClaim("customerId")).isNull();
    }
}
