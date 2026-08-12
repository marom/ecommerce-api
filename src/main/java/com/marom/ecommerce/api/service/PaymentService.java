package com.marom.ecommerce.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.PaymentResponse;
import com.marom.ecommerce.api.entity.Payment;
import com.marom.ecommerce.api.entity.PaymentStatus;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        return toResponse(findPaymentOrThrow(id));
    }

    // Only a PENDING payment can be marked COMPLETED — prevents re-completing a payment that
    // was already REFUNDED (e.g. after its order was cancelled) or marked FAILED.
    public PaymentResponse completePayment(Long id) {
        Payment payment = findPaymentOrThrow(id);
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Only a PENDING payment can be marked COMPLETED, current status is " + payment.getPaymentStatus());
        }
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        return toResponse(paymentRepository.save(payment));
    }

    private Payment findPaymentOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
