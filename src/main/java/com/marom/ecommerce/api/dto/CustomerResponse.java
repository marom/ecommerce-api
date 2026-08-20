package com.marom.ecommerce.api.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A customer")
public class CustomerResponse {

    @Schema(description = "Customer ID", example = "1")
    private Long id;
    @Schema(description = "First name", example = "Jane")
    private String firstName;
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
    @Schema(description = "Email address", example = "jane.doe@example.com")
    private String email;
    @Schema(description = "Phone number", example = "+1-555-0100")
    private String phone;
    @Schema(description = "Postal address", example = "123 Main St, Springfield")
    private String address;
    @Schema(description = "When the customer was created")
    private LocalDateTime createdAt;
}
