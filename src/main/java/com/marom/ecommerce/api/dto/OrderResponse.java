package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.marom.ecommerce.api.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
