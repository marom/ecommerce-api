package com.marom.ecommerce.api.entity;

/**
 * User roles. The enum name doubles as the Spring Security authority string
 * (e.g. {@code ROLE_ADMIN}), so {@code hasRole('ADMIN')} matches {@code ROLE_ADMIN}.
 */
public enum Role {
    ROLE_ADMIN,
    ROLE_CUSTOMER
}
