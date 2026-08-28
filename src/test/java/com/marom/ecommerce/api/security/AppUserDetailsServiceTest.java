package com.marom.ecommerce.api.security;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.marom.ecommerce.api.entity.Customer;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.entity.User;
import com.marom.ecommerce.api.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void should_returnUserDetailsWithRoleAuthority_when_userExists() {
        // Arrange
        User user = User.builder().id(2L).email("john.doe@example.com").password("hash")
                .role(Role.ROLE_CUSTOMER).customer(Customer.builder().id(1L).build()).enabled(true).build();
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails details = appUserDetailsService.loadUserByUsername("john.doe@example.com");

        // Assert
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void should_throwUsernameNotFound_when_userDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appUserDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
