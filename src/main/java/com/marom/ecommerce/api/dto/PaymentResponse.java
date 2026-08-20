package com.marom.ecommerce.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.marom.ecommerce.api.entity.PaymentMethod;
import com.marom.ecommerce.api.entity.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A payment associated with an order")
public class PaymentResponse {

    @Schema(description = "Payment ID", example = "1")
    private Long id;
    @Schema(description = "ID of the order this payment belongs to", example = "1")
    private Long orderId;
    @Schema(description = "Method used for the payment")
    private PaymentMethod paymentMethod;
    @Schema(description = "Current status of the payment")
    private PaymentStatus paymentStatus;
    @Schema(description = "Payment amount", example = "89.97")
    private BigDecimal amount;
    @Schema(description = "When the payment was created")
    private LocalDateTime createdAt;
}
