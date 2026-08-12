package com.marom.ecommerce.api.dto;

import java.util.List;

import com.marom.ecommerce.api.entity.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull
    private Long customerId;

    @NotBlank
    private String shippingAddress;

    private String notes;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
