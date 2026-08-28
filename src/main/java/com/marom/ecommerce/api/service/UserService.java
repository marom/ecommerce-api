package com.marom.ecommerce.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marom.ecommerce.api.dto.UserResponse;
import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;
import com.marom.ecommerce.api.exception.BusinessRuleException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse changeRole(Long id, Role role) {
        User user = findUserOrThrow(id);

        if (role != Role.ROLE_ADMIN && user.getRole() == Role.ROLE_ADMIN
                && userRepository.countByRole(Role.ROLE_ADMIN) <= 1) {
            throw new BusinessRuleException("Cannot demote the last remaining admin");
        }

        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private UserResponse toResponse(User user) {
        Customer customer = user.getCustomer();
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .customerId(customer != null ? customer.getId() : null)
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
