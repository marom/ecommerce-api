package com.marom.ecommerce.api.service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.OrderItemRequest;
import com.marom.ecommerce.api.dto.OrderItemResponse;
import com.marom.ecommerce.api.dto.OrderRequest;
import com.marom.ecommerce.api.dto.OrderResponse;
import com.marom.ecommerce.api.dto.PaymentResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Order;
import com.marom.ecommerce.api.entity.OrderItem;
import com.marom.ecommerce.api.entity.OrderStatus;
import com.marom.ecommerce.api.entity.Payment;
import com.marom.ecommerce.api.entity.PaymentStatus;
import com.marom.ecommerce.api.entity.Product;
import com.marom.ecommerce.api.exception.AccessDeniedException;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CustomerRepository;
import com.marom.ecommerce.api.repository.OrderRepository;
import com.marom.ecommerce.api.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    // Business rule: order status transitions are one-way. Each key lists the only statuses
    // reachable from it; DELIVERED, CANCELLED and REFUNDED have no entry and are therefore final.
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // Business rule: stock is reduced when the order is placed, not at shipment.
    // The customer is the authenticated caller, resolved by the controller.
    public OrderResponse placeOrder(Long customerId, OrderRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        Order order = Order.builder()
                .customer(customer)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .build();

        List<Long> productIds = request.getItems().stream().map(OrderItemRequest::getProductId).toList();
        Map<Long, Product> productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productsById.get(itemRequest.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id " + itemRequest.getProductId());
            }

            Product updatedProduct = productService.reduceStock(product, itemRequest.getQuantity());
            if (updatedProduct.getStockQuantity() < LOW_STOCK_THRESHOLD) {
                log.warn("Low stock alert: {} has {} units left", updatedProduct.getName(),
                        updatedProduct.getStockQuantity());
            }

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        order.setTotalAmount(total);

        Payment payment = Payment.builder()
                .paymentMethod(request.getPaymentMethod())
                .amount(total)
                .build();
        order.setPayment(payment);

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return toResponse(findOrderOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    // A customer may only read their own orders; anyone else's is a 403.
    @Transactional(readOnly = true)
    public OrderResponse getOrderForCustomer(Long id, Long customerId) {
        Order order = findOrderOrThrow(id);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Order " + id + " does not belong to the current customer");
        }
        return toResponse(order);
    }

    // Business rule: transitions must follow ALLOWED_TRANSITIONS. Cancelling restores stock for
    // every item, and if the payment had already been completed it's marked REFUNDED rather than
    // left COMPLETED against a cancelled order.
    public OrderResponse changeStatus(Long id, OrderStatus targetStatus) {
        Order order = findOrderOrThrow(id);
        Set<OrderStatus> allowedNext = ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowedNext.contains(targetStatus)) {
            throw new BusinessRuleException(
                    "Cannot transition order from " + order.getStatus() + " to " + targetStatus);
        }

        if (targetStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                productService.restoreStock(item.getProduct(), item.getQuantity());
            }
            Payment payment = order.getPayment();
            if (payment != null && payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        order.setStatus(targetStatus);
        return toResponse(orderRepository.save(order));
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .items(items)
                .payment(order.getPayment() != null ? toPaymentResponse(order.getPayment(), order.getId()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment payment, Long orderId) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(orderId)
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
