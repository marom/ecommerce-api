package com.marom.ecommerce.api.dto;

import com.marom.ecommerce.api.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to change a user's role")
public class ChangeRoleRequest {

    @Schema(description = "New role to assign", example = "ROLE_ADMIN")
    @NotNull
    private Role role;
}
