package com.marom.ecommerce.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marom.ecommerce.api.dto.ErrorResponse;
import com.marom.ecommerce.api.dto.OrderRequest;
import com.marom.ecommerce.api.dto.OrderResponse;
import com.marom.ecommerce.api.dto.OrderStatusRequest;
import com.marom.ecommerce.api.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Place and manage orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place an order", description = "Reduces stock for each ordered product and creates a pending payment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Customer or product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Insufficient stock for a requested product",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> get(@Parameter(description = "Order ID") @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    @Operation(summary = "List all orders")
    @ApiResponse(responseCode = "200", description = "Orders retrieved")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Change an order's status",
            description = "Status transitions are one-way (e.g. PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED). "
                    + "Cancelling restores stock for every item and refunds a completed payment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Status transition not allowed from the order's current status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OrderResponse> changeStatus(@Parameter(description = "Order ID") @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.changeStatus(id, request.getStatus()));
    }
}
