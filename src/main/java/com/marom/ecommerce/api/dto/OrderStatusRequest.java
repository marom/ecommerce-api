package com.marom.ecommerce.api.dto;

import com.marom.ecommerce.api.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequest {

    @NotNull
    private OrderStatus status;
}
