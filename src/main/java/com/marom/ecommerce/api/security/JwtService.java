package com.marom.ecommerce.api.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.marom.ecommerce.api.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * Mints signed HS256 JWTs for authenticated users. The token carries everything the
 * resource-server side needs to build an {@code Authentication} without a DB hit:
 * {@code sub} (email), {@code roles}, {@code uid}, and {@code customerId} (absent for admins).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.security.jwt.issuer}")
    private final String issuer;

    @Value("${app.security.jwt.expiration-seconds}")
    private final long expirationSeconds;

    public String issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(expirationSeconds, ChronoUnit.SECONDS))
                .subject(user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .claim("uid", user.getId());

        if (user.getCustomer() != null) {
            claims.claim("customerId", user.getCustomer().getId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public long expiresInSeconds() {
        return expirationSeconds;
    }
}
