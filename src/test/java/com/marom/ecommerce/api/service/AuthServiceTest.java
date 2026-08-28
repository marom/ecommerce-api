package com.marom.ecommerce.api.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.marom.ecommerce.api.dto.CurrentUserResponse;
import com.marom.ecommerce.api.dto.LoginRequest;
import com.marom.ecommerce.api.dto.RegisterRequest;
import com.marom.ecommerce.api.dto.TokenResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.repository.CustomerRepository;
import com.marom.ecommerce.api.repository.UserRepository;
import com.marom.ecommerce.api.security.CurrentUserService;
import com.marom.ecommerce.api.security.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthService authService;

    private static User customerUser() {
        return User.builder().id(2L).email("john.doe@example.com").password("hash")
                .role(Role.ROLE_CUSTOMER)
                .customer(Customer.builder().id(1L).firstName("John").lastName("Doe").build())
                .enabled(true).build();
    }

    @Test
    void should_returnToken_when_loginSucceeds() {
        // Arrange
        LoginRequest request = LoginRequest.builder().email("john.doe@example.com").password("password123").build();
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(customerUser()));
        when(jwtService.issue(any(User.class))).thenReturn("signed-token");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        // Act
        TokenResponse response = authService.login(request);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("signed-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        assertThat(response.getRole()).isEqualTo(Role.ROLE_CUSTOMER);
        assertThat(response.getCustomerId()).isEqualTo(1L);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void should_propagateBadCredentials_when_authenticationFails() {
        // Arrange
        LoginRequest request = LoginRequest.builder().email("john.doe@example.com").password("wrong").build();
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).issue(any());
    }

    @Test
    void should_createLinkedCustomerAndUser_when_registerSucceeds() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new.customer@example.com").password("s3cure-pass")
                .firstName("New").lastName("Customer").phone("+1-555-0000").address("1 New St").build();
        when(userRepository.existsByEmail("new.customer@example.com")).thenReturn(false);
        when(customerRepository.findByEmail("new.customer@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("s3cure-pass")).thenReturn("encoded-hash");
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Customer.class));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0, User.class);
            u.setId(9L);
            return u;
        });
        when(jwtService.issue(any(User.class))).thenReturn("signed-token");
        when(jwtService.expiresInSeconds()).thenReturn(3600L);

        // Act
        TokenResponse response = authService.register(request);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("signed-token");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded-hash");
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ROLE_CUSTOMER);
        assertThat(userCaptor.getValue().getCustomer()).isNotNull();
        assertThat(userCaptor.getValue().isEnabled()).isTrue();
    }

    @Test
    void should_throwDuplicateResourceException_when_registeringExistingEmail() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("john.doe@example.com").password("s3cure-pass")
                .firstName("John").lastName("Doe").build();
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john.doe@example.com");
        verify(userRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void should_mapCurrentUser_when_meCalled() {
        // Arrange
        when(currentUserService.currentEmail()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(customerUser()));

        // Act
        CurrentUserResponse response = authService.me();

        // Assert
        assertThat(response.getUserId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_CUSTOMER);
        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
    }
}
