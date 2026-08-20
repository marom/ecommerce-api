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
@Schema(description = "A product category")
public class CategoryResponse {

    @Schema(description = "Category ID", example = "1")
    private Long id;
    @Schema(description = "Category name", example = "Electronics")
    private String name;
    @Schema(description = "URL-friendly identifier", example = "electronics")
    private String slug;
    @Schema(description = "Category description", example = "Devices, gadgets and accessories")
    private String description;
    @Schema(description = "When the category was created")
    private LocalDateTime createdAt;
    @Schema(description = "When the category was last updated")
    private LocalDateTime updatedAt;
}
