package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.marom.ecommerce.api.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "An order")
public class OrderResponse {

    @Schema(description = "Order ID", example = "1")
    private Long id;
    @Schema(description = "Human-readable order number", example = "ORD-20260812-0001")
    private String orderNumber;
    @Schema(description = "ID of the customer who placed the order", example = "1")
    private Long customerId;
    @Schema(description = "Current status of the order")
    private OrderStatus status;
    @Schema(description = "Total amount for the order", example = "89.97")
    private BigDecimal totalAmount;
    @Schema(description = "Address the order will be shipped to", example = "123 Main St, Springfield")
    private String shippingAddress;
    @Schema(description = "Optional notes for the order", example = "Leave at the front door")
    private String notes;
    @Schema(description = "Line items in the order")
    private List<OrderItemResponse> items;
    @Schema(description = "Payment associated with the order")
    private PaymentResponse payment;
    @Schema(description = "When the order was created")
    private LocalDateTime createdAt;
    @Schema(description = "When the order was last updated")
    private LocalDateTime updatedAt;
}
