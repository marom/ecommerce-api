package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create or update a product")
public class ProductRequest {

    @Schema(description = "Product name", example = "Wireless Mouse")
    @NotBlank
    @Size(max = 200)
    private String name;

    @Schema(description = "Product description", example = "A wireless mouse with ergonomic design")
    private String description;

    @Schema(description = "Unit price, must be greater than zero", example = "29.99")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @Schema(description = "Stock keeping unit, must be unique across products", example = "WM-1000")
    @NotBlank
    @Size(max = 100)
    private String sku;

    @Schema(description = "Units currently in stock", example = "150")
    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @Schema(description = "Whether the product is visible/purchasable", example = "true")
    @NotNull
    private Boolean active;

    @Schema(description = "ID of the category this product belongs to", example = "1")
    @NotNull
    private Long categoryId;
}
