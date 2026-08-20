package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single line item within an order")
public class OrderItemResponse {

    @Schema(description = "Order item ID", example = "1")
    private Long id;
    @Schema(description = "ID of the ordered product", example = "1")
    private Long productId;
    @Schema(description = "Name of the ordered product", example = "Wireless Mouse")
    private String productName;
    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;
    @Schema(description = "Unit price at the time the order was placed", example = "29.99")
    private BigDecimal unitPrice;
    @Schema(description = "quantity * unitPrice", example = "59.98")
    private BigDecimal subtotal;
}
