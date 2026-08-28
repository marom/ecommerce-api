package com.marom.ecommerce.api.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marom.ecommerce.api.dto.UserResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void should_returnMappedUsers_when_listUsers() {
        // Arrange
        User admin = User.builder().id(1L).email("admin@shop.example.com").role(Role.ROLE_ADMIN).enabled(true).build();
        User customer = User.builder().id(2L).email("john.doe@example.com").role(Role.ROLE_CUSTOMER)
                .customer(Customer.builder().id(1L).build()).enabled(true).build();
        when(userRepository.findAll()).thenReturn(List.of(admin, customer));

        // Act
        List<UserResponse> responses = userService.listUsers();

        // Assert
        assertThat(responses).extracting(UserResponse::getEmail)
                .containsExactly("admin@shop.example.com", "john.doe@example.com");
    }

    @Test
    void should_changeRole_when_notDemotingLastAdmin() {
        // Arrange
        User user = User.builder().id(2L).email("john.doe@example.com").role(Role.ROLE_CUSTOMER).enabled(true).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse response = userService.changeRole(2L, Role.ROLE_ADMIN);

        // Assert
        assertThat(response.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void should_throwResourceNotFound_when_userDoesNotExist() {
        // Arrange
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.changeRole(404L, Role.ROLE_ADMIN))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void should_throwBusinessRuleException_when_demotingLastAdmin() {
        // Arrange
        User admin = User.builder().id(1L).email("admin@shop.example.com").role(Role.ROLE_ADMIN).enabled(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRole(Role.ROLE_ADMIN)).thenReturn(1L);

        // Act & Assert
        assertThatThrownBy(() -> userService.changeRole(1L, Role.ROLE_CUSTOMER))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("last remaining admin");
        verify(userRepository, never()).save(any());
    }
}
