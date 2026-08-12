package com.marom.ecommerce.api.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marom.ecommerce.api.dto.OrderItemRequest;
import com.marom.ecommerce.api.dto.OrderRequest;
import com.marom.ecommerce.api.dto.OrderResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Order;
import com.marom.ecommerce.api.entity.OrderItem;
import com.marom.ecommerce.api.entity.OrderStatus;
import com.marom.ecommerce.api.entity.Payment;
import com.marom.ecommerce.api.entity.PaymentMethod;
import com.marom.ecommerce.api.entity.PaymentStatus;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CustomerRepository;
import com.marom.ecommerce.api.repository.OrderRepository;
import com.marom.ecommerce.api.repository.ProductRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private static Customer customer() {
        return Customer.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com").build();
    }

    @Test
    void should_placeOrder_when_requestIsValid() {
        // Arrange
        Product mouse = Product.builder().id(1L).name("Wireless Mouse").price(new BigDecimal("24.99")).stockQuantity(150).build();
        Product keyboard = Product.builder().id(2L).name("Mechanical Keyboard").price(new BigDecimal("79.99")).stockQuantity(80).build();
        OrderRequest request = OrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Maple Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(
                        OrderItemRequest.builder().productId(1L).quantity(2).build(),
                        OrderItemRequest.builder().productId(2L).quantity(1).build()))
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mouse));
        when(productRepository.findById(2L)).thenReturn(Optional.of(keyboard));
        when(productService.reduceStock(mouse, 2)).thenReturn(mouse);
        when(productService.reduceStock(keyboard, 1)).thenReturn(keyboard);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.placeOrder(request);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("129.97"));
        assertThat(response.getItems()).hasSize(2);
        verify(productService).reduceStock(mouse, 2);
        verify(productService).reduceStock(keyboard, 1);
    }

    @Test
    void should_throwResourceNotFoundException_when_customerNotFound() {
        // Arrange
        OrderRequest request = OrderRequest.builder()
                .customerId(404L)
                .shippingAddress("123 Maple Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(1).build()))
                .build();
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(productRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_throwResourceNotFoundException_when_productNotFound() {
        // Arrange
        OrderRequest request = OrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Maple Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(404L).quantity(1).build()))
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_stockInsufficient() {
        // Arrange
        Product product = Product.builder().id(1L).name("Wireless Mouse").price(new BigDecimal("24.99")).stockQuantity(2).build();
        OrderRequest request = OrderRequest.builder()
                .customerId(1L)
                .shippingAddress("123 Maple Street")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .items(List.of(OrderItemRequest.builder().productId(1L).quantity(10).build()))
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productService.reduceStock(product, 10))
                .thenThrow(new BusinessRuleException("Insufficient stock for product 'Wireless Mouse': requested 10, available 2"));

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_returnOrder_when_orderExists() {
        // Arrange
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST0001")
                .customer(customer())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .shippingAddress("123 Test St")
                .items(new ArrayList<>())
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrder(1L);

        // Assert
        assertThat(response.getOrderNumber()).isEqualTo("ORD-TEST0001");
    }

    @Test
    void should_throwResourceNotFoundException_when_orderDoesNotExist() {
        // Arrange
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrder(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void should_returnAllOrders_when_ordersExist() {
        // Arrange
        Order first = Order.builder().id(1L).orderNumber("ORD-AAAA0001").customer(customer())
                .status(OrderStatus.PENDING).totalAmount(BigDecimal.ZERO).shippingAddress("123 Test St").items(new ArrayList<>()).build();
        Order second = Order.builder().id(2L).orderNumber("ORD-BBBB0002").customer(customer())
                .status(OrderStatus.PENDING).totalAmount(BigDecimal.ZERO).shippingAddress("123 Test St").items(new ArrayList<>()).build();
        when(orderRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<OrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertThat(responses).extracting(OrderResponse::getOrderNumber).containsExactly("ORD-AAAA0001", "ORD-BBBB0002");
    }

    @Test
    void should_confirmOrder_when_currentStatusIsPending() {
        // Arrange
        Order order = Order.builder().id(1L).orderNumber("ORD-TEST0001").customer(customer())
                .status(OrderStatus.PENDING).totalAmount(new BigDecimal("100.00"))
                .shippingAddress("123 Test St").items(new ArrayList<>()).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.changeStatus(1L, OrderStatus.CONFIRMED);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verifyNoInteractions(productService);
    }

    @Test
    void should_throwBusinessRuleException_when_transitionIsNotAllowed() {
        // Arrange
        Order order = Order.builder().id(1L).customer(customer()).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO).shippingAddress("123 Test St").items(new ArrayList<>()).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.changeStatus(1L, OrderStatus.SHIPPED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("SHIPPED");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_cancelOrderAndRefundPayment_when_paymentWasCompleted() {
        // Arrange
        Product mouse = Product.builder().id(1L).name("Wireless Mouse").stockQuantity(148).build();
        Product keyboard = Product.builder().id(2L).name("Mechanical Keyboard").stockQuantity(79).build();
        OrderItem item1 = OrderItem.builder().id(1L).product(mouse).quantity(2).unitPrice(new BigDecimal("24.99")).build();
        OrderItem item2 = OrderItem.builder().id(2L).product(keyboard).quantity(1).unitPrice(new BigDecimal("79.99")).build();
        Payment payment = Payment.builder().id(1L).paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED).amount(new BigDecimal("129.97")).build();
        Order order = Order.builder().id(1L).customer(customer()).status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("129.97")).shippingAddress("123 Test St")
                .items(new ArrayList<>(List.of(item1, item2))).payment(payment).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.changeStatus(1L, OrderStatus.CANCELLED);

        // Assert
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.getPayment().getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(productService).restoreStock(mouse, 2);
        verify(productService).restoreStock(keyboard, 1);
    }

    @Test
    void should_cancelOrderWithoutRefund_when_paymentWasNotCompleted() {
        // Arrange
        Product mouse = Product.builder().id(1L).name("Wireless Mouse").stockQuantity(148).build();
        OrderItem item = OrderItem.builder().id(1L).product(mouse).quantity(2).unitPrice(new BigDecimal("24.99")).build();
        Payment payment = Payment.builder().id(1L).paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.PENDING).amount(new BigDecimal("49.98")).build();
        Order order = Order.builder().id(1L).customer(customer()).status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("49.98")).shippingAddress("123 Test St")
                .items(new ArrayList<>(List.of(item))).payment(payment).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.changeStatus(1L, OrderStatus.CANCELLED);

        // Assert
        assertThat(response.getPayment().getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(productService).restoreStock(mouse, 2);
    }

    @Test
    void should_throwBusinessRuleException_when_cancellingAlreadyDeliveredOrder() {
        // Arrange
        Order order = Order.builder().id(1L).customer(customer()).status(OrderStatus.DELIVERED)
                .totalAmount(BigDecimal.ZERO).shippingAddress("123 Test St").items(new ArrayList<>()).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.changeStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DELIVERED");
        verifyNoInteractions(productService);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void should_throwBusinessRuleException_when_cancellingAlreadyCancelledOrder() {
        // Arrange
        Order order = Order.builder().id(1L).customer(customer()).status(OrderStatus.CANCELLED)
                .totalAmount(BigDecimal.ZERO).shippingAddress("123 Test St").items(new ArrayList<>()).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.changeStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CANCELLED");
        verifyNoInteractions(productService);
        verify(orderRepository, never()).save(any());
    }
}
