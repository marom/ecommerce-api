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
@Schema(description = "The currently authenticated user")
public class CurrentUserResponse {

    @Schema(description = "User ID", example = "2")
    private Long userId;
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    @Schema(description = "Assigned role", example = "ROLE_CUSTOMER")
    private Role role;
    @Schema(description = "Linked customer ID, null for admin accounts", example = "1")
    private Long customerId;
    @Schema(description = "First name, null for admin accounts", example = "John")
    private String firstName;
    @Schema(description = "Last name, null for admin accounts", example = "Doe")
    private String lastName;
}
