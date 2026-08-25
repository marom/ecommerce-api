package com.marom.ecommerce.api.controller;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.marom.ecommerce.api.dto.PaymentResponse;
import com.marom.ecommerce.api.entity.PaymentMethod;
import com.marom.ecommerce.api.entity.PaymentStatus;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.PaymentService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PaymentControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    PaymentControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private PaymentResponse samplePaymentResponse() {
        return PaymentResponse.builder()
                .id(1L)
                .orderId(1L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING)
                .amount(BigDecimal.valueOf(59.98))
                .build();
    }

    // ----- GET /api/v1/payments/{id} -----

    @Test
    void should_returnOk_when_paymentExists() throws Exception {
        // Arrange
        when(paymentService.getPayment(1L)).thenReturn(samplePaymentResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
    }

    @Test
    void should_returnNotFound_when_paymentDoesNotExistOnGet() throws Exception {
        // Arrange
        when(paymentService.getPayment(404L)).thenThrow(new ResourceNotFoundException("Payment not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/404"))
                .andExpect(status().isNotFound());
    }

    // ----- PUT /api/v1/payments/{id}/complete -----

    @Test
    void should_returnOk_when_paymentIsCompleted() throws Exception {
        // Arrange
        PaymentResponse response = samplePaymentResponse();
        response.setPaymentStatus(PaymentStatus.COMPLETED);
        when(paymentService.completePayment(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/payments/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("COMPLETED"));
    }

    @Test
    void should_returnNotFound_when_paymentDoesNotExistOnComplete() throws Exception {
        // Arrange
        when(paymentService.completePayment(404L)).thenThrow(new ResourceNotFoundException("Payment not found with id 404"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/payments/404/complete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnUnprocessableEntity_when_paymentIsNotPending() throws Exception {
        // Arrange
        when(paymentService.completePayment(1L))
                .thenThrow(new BusinessRuleException("Only a PENDING payment can be marked COMPLETED, current status is COMPLETED"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/payments/1/complete"))
                .andExpect(status().isUnprocessableEntity());
    }
}
