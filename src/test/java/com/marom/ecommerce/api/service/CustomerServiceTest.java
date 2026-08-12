package com.marom.ecommerce.api.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marom.ecommerce.api.dto.CustomerRequest;
import com.marom.ecommerce.api.dto.CustomerResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CustomerRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void should_registerCustomer_when_emailIsAvailable() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        Customer saved = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        // Act
        CustomerResponse response = customerService.createCustomer(request);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void should_throwDuplicateResourceException_when_emailAlreadyExists() {
        // Arrange
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        Customer existing = Customer.builder().id(9L).email("john.doe@example.com").build();
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john.doe@example.com");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void should_returnCustomer_when_customerExists() {
        // Arrange
        Customer customer = Customer.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com").build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Act
        CustomerResponse response = customerService.getCustomer(1L);

        // Assert
        assertThat(response.getFirstName()).isEqualTo("John");
    }

    @Test
    void should_throwResourceNotFoundException_when_customerDoesNotExist() {
        // Arrange
        when(customerRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomer(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void should_returnAllCustomers_when_customersExist() {
        // Arrange
        Customer first = Customer.builder().id(1L).firstName("John").email("john.doe@example.com").build();
        Customer second = Customer.builder().id(2L).firstName("Jane").email("jane.smith@example.com").build();
        when(customerRepository.findAll()).thenReturn(List.of(first, second));

        // Act
        List<CustomerResponse> responses = customerService.getAllCustomers();

        // Assert
        assertThat(responses).extracting(CustomerResponse::getFirstName).containsExactly("John", "Jane");
    }

    @Test
    void should_updateCustomer_when_emailBelongsToSameCustomer() {
        // Arrange
        Customer existing = Customer.builder().id(1L).firstName("John").lastName("Doe").email("john.doe@example.com").build();
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Johnny")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CustomerResponse response = customerService.updateCustomer(1L, request);

        // Assert
        assertThat(response.getFirstName()).isEqualTo("Johnny");
    }

    @Test
    void should_deleteCustomer_when_customerExists() {
        // Arrange
        Customer customer = Customer.builder().id(1L).email("john.doe@example.com").build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository).delete(customer);
    }
}
