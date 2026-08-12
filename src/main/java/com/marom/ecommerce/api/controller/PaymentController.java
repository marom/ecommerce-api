package com.marom.ecommerce.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marom.ecommerce.api.dto.PaymentResponse;
import com.marom.ecommerce.api.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<PaymentResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.completePayment(id));
    }
}
