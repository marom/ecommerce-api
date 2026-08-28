package com.marom.ecommerce.api.dto;

import java.time.LocalDateTime;

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
@Schema(description = "A user account")
public class UserResponse {

    @Schema(description = "User ID", example = "2")
    private Long id;
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    @Schema(description = "Assigned role", example = "ROLE_CUSTOMER")
    private Role role;
    @Schema(description = "Linked customer ID, null for admin accounts", example = "1")
    private Long customerId;
    @Schema(description = "Whether the account can authenticate", example = "true")
    private boolean enabled;
    @Schema(description = "When the account was created")
    private LocalDateTime createdAt;
}
