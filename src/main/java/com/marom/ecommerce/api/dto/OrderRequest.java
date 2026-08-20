package com.marom.ecommerce.api.dto;

import java.util.List;

import com.marom.ecommerce.api.entity.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request payload to place an order")
public class OrderRequest {

    @Schema(description = "ID of the customer placing the order", example = "1")
    @NotNull
    private Long customerId;

    @Schema(description = "Address the order will be shipped to", example = "123 Main St, Springfield")
    @NotBlank
    private String shippingAddress;

    @Schema(description = "Optional notes for the order", example = "Leave at the front door")
    private String notes;

    @Schema(description = "Payment method used for the order")
    @NotNull
    private PaymentMethod paymentMethod;

    @Schema(description = "Line items in the order, at least one required")
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
