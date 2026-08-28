package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A product in the catalog")
public class ProductResponse {

    @Schema(description = "Product ID", example = "1")
    private Long id;
    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;
    @Schema(description = "Product description", example = "A wireless mouse with ergonomic design")
    private String description;
    @Schema(description = "Unit price", example = "29.99")
    private BigDecimal price;
    @Schema(description = "Stock keeping unit", example = "WM-1000")
    private String sku;
    @Schema(description = "Units currently in stock", example = "150")
    private Integer stockQuantity;
    @Schema(description = "Whether the product is visible/purchasable", example = "true")
    private Boolean active;
    @Schema(description = "ID of the category this product belongs to", example = "1")
    private Long categoryId;
    @Schema(description = "Name of the category this product belongs to", example = "Electronics")
    private String categoryName;
    @Schema(description = "Pictures attached to this product, ordered by display order (first = primary)")
    private List<ProductPictureResponse> pictures;
    @Schema(description = "When the product was created")
    private LocalDateTime createdAt;
    @Schema(description = "When the product was last updated")
    private LocalDateTime updatedAt;
}
