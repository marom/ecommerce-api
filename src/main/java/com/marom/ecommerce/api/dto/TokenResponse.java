package com.marom.ecommerce.api.dto;

import com.marom.ecommerce.api.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issued access token and the identity it represents")
public class TokenResponse {

    @Schema(description = "Signed JWT to send as 'Authorization: Bearer <token>'")
    private String accessToken;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Seconds until the token expires", example = "3600")
    private long expiresIn;

    @Schema(description = "Role granted to the token holder", example = "ROLE_CUSTOMER")
    private Role role;

    @Schema(description = "Linked customer ID, null for admin accounts", example = "1")
    private Long customerId;
}
