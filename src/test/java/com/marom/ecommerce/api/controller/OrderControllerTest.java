package com.marom.ecommerce.api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.OrderItemRequest;
import com.marom.ecommerce.api.dto.OrderItemResponse;
import com.marom.ecommerce.api.dto.OrderRequest;
import com.marom.ecommerce.api.dto.OrderResponse;
import com.marom.ecommerce.api.dto.OrderStatusRequest;
import com.marom.ecommerce.api.entity.OrderStatus;
import com.marom.ecommerce.api.entity.PaymentMethod;
import com.marom.ecommerce.api.exception.AccessDeniedException;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.OrderService;
import com.marom.ecommerce.api.support.SecurityTestSupport;
import com.marom.ecommerce.api.support.WithMockCustomUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WithMockCustomUser(customerId = 1L)
class OrderControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    OrderControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private OrderRequest validOrderRequest() {
        OrderItemRequest item = OrderItemRequest.builder().productId(1L).quantity(2).build();
        return OrderRequest.builder()
                .shippingAddress("123 Main St, Springfield")
                .notes("Leave at the front door")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(item))
                .build();
    }

    private OrderResponse sampleOrderResponse() {
        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .id(1L).productId(1L).productName("Wireless Mouse")
                .quantity(2).unitPrice(BigDecimal.valueOf(29.99)).subtotal(BigDecimal.valueOf(59.98))
                .build();
        return OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-20260812-0001")
                .customerId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(59.98))
                .shippingAddress("123 Main St, Springfield")
                .notes("Leave at the front door")
                .items(List.of(itemResponse))
                .build();
    }

    // ----- POST /api/v1/orders -----

    @Test
    void should_returnCreated_when_orderRequestIsValid() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        when(orderService.placeOrder(eq(1L), any(OrderRequest.class))).thenReturn(sampleOrderResponse());

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20260812-0001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(59.98));
    }

    @Test
    void should_returnBadRequest_when_shippingAddressIsBlank() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        request.setShippingAddress("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_paymentMethodIsMissing() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        request.setPaymentMethod(null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_itemsIsEmpty() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        request.setItems(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_itemProductIdIsMissing() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        request.setItems(List.of(OrderItemRequest.builder().quantity(2).build()));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_itemQuantityIsMissing() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        request.setItems(List.of(OrderItemRequest.builder().productId(1L).build()));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_itemQuantityIsBelowMinimum() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        request.setItems(List.of(OrderItemRequest.builder().productId(1L).quantity(0).build()));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnNotFound_when_customerDoesNotExistOnPlaceOrder() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        when(orderService.placeOrder(eq(1L), any(OrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 1"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnUnprocessableEntity_when_stockIsInsufficientOnPlaceOrder() throws Exception {
        // Arrange
        OrderRequest request = validOrderRequest();
        when(orderService.placeOrder(eq(1L), any(OrderRequest.class)))
                .thenThrow(new BusinessRuleException("Insufficient stock for product with id 1"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithAnonymousUser
    void should_return401_when_noAuthOnPlaceOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_return403_when_adminPlacesOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest())))
                .andExpect(status().isForbidden());
    }

    // ----- GET /api/v1/orders/{id} -----

    @Test
    void should_returnOk_when_customerGetsOwnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderForCustomer(1L, 1L)).thenReturn(sampleOrderResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-20260812-0001"));
    }

    @Test
    void should_returnNotFound_when_orderDoesNotExistOnGet() throws Exception {
        // Arrange
        when(orderService.getOrderForCustomer(404L, 1L))
                .thenThrow(new ResourceNotFoundException("Order not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnForbidden_when_customerGetsAnotherCustomersOrder() throws Exception {
        // Arrange
        when(orderService.getOrderForCustomer(2L, 1L))
                .thenThrow(new AccessDeniedException("Order 2 does not belong to the current customer"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnOk_when_adminGetsAnyOrder() throws Exception {
        // Arrange
        when(orderService.getOrder(1L)).thenReturn(sampleOrderResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ----- GET /api/v1/orders -----

    @Test
    void should_returnOwnOrders_when_customerListsOrders() throws Exception {
        // Arrange
        when(orderService.getOrdersForCustomer(1L)).thenReturn(List.of(sampleOrderResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-20260812-0001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnAllOrders_when_adminListsOrders() throws Exception {
        // Arrange
        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrderResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ----- PUT /api/v1/orders/{id}/status -----

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnOk_when_statusChangeIsValid() throws Exception {
        // Arrange
        OrderStatusRequest request = OrderStatusRequest.builder().status(OrderStatus.CONFIRMED).build();
        OrderResponse response = sampleOrderResponse();
        response.setStatus(OrderStatus.CONFIRMED);
        when(orderService.changeStatus(eq(1L), eq(OrderStatus.CONFIRMED))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnBadRequest_when_statusIsMissing() throws Exception {
        // Arrange
        OrderStatusRequest request = OrderStatusRequest.builder().build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnNotFound_when_orderDoesNotExistOnStatusChange() throws Exception {
        // Arrange
        OrderStatusRequest request = OrderStatusRequest.builder().status(OrderStatus.CONFIRMED).build();
        when(orderService.changeStatus(eq(404L), any(OrderStatus.class)))
                .thenThrow(new ResourceNotFoundException("Order not found with id 404"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/404/status").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnUnprocessableEntity_when_statusTransitionIsNotAllowed() throws Exception {
        // Arrange
        OrderStatusRequest request = OrderStatusRequest.builder().status(OrderStatus.DELIVERED).build();
        when(orderService.changeStatus(eq(1L), eq(OrderStatus.DELIVERED)))
                .thenThrow(new BusinessRuleException("Cannot transition order from PENDING to DELIVERED"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void should_returnForbidden_when_customerChangesOrderStatus() throws Exception {
        // Arrange
        OrderStatusRequest request = OrderStatusRequest.builder().status(OrderStatus.CONFIRMED).build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
