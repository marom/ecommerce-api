package com.marom.ecommerce.api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.CurrentUserResponse;
import com.marom.ecommerce.api.dto.LoginRequest;
import com.marom.ecommerce.api.dto.RegisterRequest;
import com.marom.ecommerce.api.dto.TokenResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.CustomerRepository;
import com.marom.ecommerce.api.repository.UserRepository;
import com.marom.ecommerce.api.security.CurrentUserService;
import com.marom.ecommerce.api.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public TokenResponse login(LoginRequest request) {
        // Bad credentials / disabled account propagate to GlobalExceptionHandler as 401.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + request.getEmail()));
        return toTokenResponse(user);
    }

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())
                || customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Account with email '" + request.getEmail() + "' already exists");
        }

        Customer customer = customerRepository.save(Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());

        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_CUSTOMER)
                .customer(customer)
                .enabled(true)
                .build());

        return toTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse me() {
        User user = userRepository.findByEmail(currentUserService.currentEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user no longer exists"));
        Customer customer = user.getCustomer();
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .customerId(customer != null ? customer.getId() : null)
                .firstName(customer != null ? customer.getFirstName() : null)
                .lastName(customer != null ? customer.getLastName() : null)
                .build();
    }

    private TokenResponse toTokenResponse(User user) {
        Customer customer = user.getCustomer();
        return TokenResponse.builder()
                .accessToken(jwtService.issue(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.expiresInSeconds())
                .role(user.getRole())
                .customerId(customer != null ? customer.getId() : null)
                .build();
    }
}
