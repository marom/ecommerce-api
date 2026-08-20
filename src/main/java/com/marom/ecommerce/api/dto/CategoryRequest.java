package com.marom.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload to create or update a category")
public class CategoryRequest {

    @Schema(description = "Category name, must be unique", example = "Electronics")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "URL-friendly identifier, must be unique", example = "electronics")
    @NotBlank
    @Size(max = 100)
    private String slug;

    @Schema(description = "Category description", example = "Devices, gadgets and accessories")
    private String description;
}
