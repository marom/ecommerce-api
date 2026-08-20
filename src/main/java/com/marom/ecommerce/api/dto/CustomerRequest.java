package com.marom.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create or update a customer")
public class CustomerRequest {

    @Schema(description = "First name", example = "Jane")
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Schema(description = "Email address, must be unique", example = "jane.doe@example.com")
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @Schema(description = "Phone number", example = "+1-555-0100")
    @Size(max = 20)
    private String phone;

    @Schema(description = "Postal address", example = "123 Main St, Springfield")
    private String address;
}
