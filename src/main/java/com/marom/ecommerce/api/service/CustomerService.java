package com.marom.ecommerce.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.CustomerRequest;
import com.marom.ecommerce.api.dto.CustomerResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest request) {
        assertEmailAvailable(request.getEmail(), null);

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return toResponse(findCustomerOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);
        assertEmailAvailable(request.getEmail(), id);

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        return toResponse(customerRepository.save(customer));
    }

    public void deleteCustomer(Long id) {
        customerRepository.delete(findCustomerOrThrow(id));
    }

    private void assertEmailAvailable(String email, Long selfId) {
        customerRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Customer with email '" + email + "' already exists");
                });
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
