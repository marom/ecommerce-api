package com.marom.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credentials to obtain an access token")
public class LoginRequest {

    @Schema(description = "Registered email address", example = "john.doe@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Account password", example = "password123")
    @NotBlank
    private String password;
}
