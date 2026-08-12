package com.marom.ecommerce.api.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marom.ecommerce.api.dto.PaymentResponse;
import com.marom.ecommerce.api.entity.Order;
import com.marom.ecommerce.api.entity.Payment;
import com.marom.ecommerce.api.entity.PaymentMethod;
import com.marom.ecommerce.api.entity.PaymentStatus;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.PaymentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private static Order order() {
        return Order.builder().id(1L).build();
    }

    @Test
    void should_returnPayment_when_paymentExists() {
        // Arrange
        Payment payment = Payment.builder().id(1L).order(order()).paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING).amount(new BigDecimal("129.97")).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = paymentService.getPayment(1L);

        // Assert
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void should_throwResourceNotFoundException_when_paymentDoesNotExist() {
        // Arrange
        when(paymentRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPayment(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void should_completePayment_when_statusIsPending() {
        // Arrange
        Payment payment = Payment.builder().id(1L).order(order()).paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING).amount(new BigDecimal("129.97")).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentResponse response = paymentService.completePayment(1L);

        // Assert
        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void should_throwBusinessRuleException_when_paymentAlreadyCompleted() {
        // Arrange
        Payment payment = Payment.builder().id(1L).order(order()).paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED).amount(new BigDecimal("129.97")).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.completePayment(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDING");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_paymentAlreadyRefunded() {
        // Arrange
        Payment payment = Payment.builder().id(1L).order(order()).paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.REFUNDED).amount(new BigDecimal("129.97")).build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.completePayment(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("REFUNDED");
        verify(paymentRepository, never()).save(any());
    }
}
