package com.marom.ecommerce.api.dto;

import com.marom.ecommerce.api.entity.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to change an order's status")
public class OrderStatusRequest {

    @Schema(description = "Target status; must be a valid transition from the order's current status", example = "CONFIRMED")
    @NotNull
    private OrderStatus status;
}
