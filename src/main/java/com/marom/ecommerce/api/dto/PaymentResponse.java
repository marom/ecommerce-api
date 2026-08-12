package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.marom.ecommerce.api.entity.PaymentMethod;
import com.marom.ecommerce.api.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
