package com.marom.ecommerce.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single line item requested within an order")
public class OrderItemRequest {

    @Schema(description = "ID of the product being ordered", example = "1")
    @NotNull
    private Long productId;

    @Schema(description = "Quantity ordered, must be at least 1", example = "2")
    @NotNull
    @Min(1)
    private Integer quantity;
}
