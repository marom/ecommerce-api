package com.marom.ecommerce.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "customer")
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "customer")
    List<User> findAll();

    boolean existsByEmail(String email);

    long countByRole(Role role);
}
