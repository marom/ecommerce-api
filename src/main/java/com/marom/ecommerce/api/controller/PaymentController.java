package com.marom.ecommerce.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marom.ecommerce.api.dto.ErrorResponse;
import com.marom.ecommerce.api.dto.PaymentResponse;
import com.marom.ecommerce.api.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "View and complete payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a payment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> get(@Parameter(description = "Payment ID") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete a payment", description = "Only a PENDING payment can be marked COMPLETED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment completed"),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Payment is not in PENDING status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> complete(@Parameter(description = "Payment ID") @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.completePayment(id));
    }
}
