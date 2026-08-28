package com.marom.ecommerce.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.CurrentUserResponse;
import com.marom.ecommerce.api.dto.LoginRequest;
import com.marom.ecommerce.api.dto.RegisterRequest;
import com.marom.ecommerce.api.dto.TokenResponse;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.service.AuthService;
import com.marom.ecommerce.api.support.SecurityTestSupport;
import com.marom.ecommerce.api.support.WithMockCustomUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AuthControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    AuthControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private TokenResponse tokenResponse() {
        return TokenResponse.builder()
                .accessToken("signed-token").tokenType("Bearer").expiresIn(3600L)
                .role(Role.ROLE_CUSTOMER).customerId(1L).build();
    }

    @Test
    void should_returnOkWithToken_when_loginRequestIsValid() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder().email("john.doe@example.com").password("password123").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse());

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed-token"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));
    }

    @Test
    void should_returnBadRequest_when_loginEmailIsBlank() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder().email("").password("password123").build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnUnauthorized_when_credentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest request = LoginRequest.builder().email("john.doe@example.com").password("wrong").build();
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("bad"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_returnCreatedWithToken_when_registerRequestIsValid() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new.customer@example.com").password("s3cure-pass")
                .firstName("New").lastName("Customer").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(tokenResponse());

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("signed-token"));
    }

    @Test
    void should_returnConflict_when_registerEmailAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("john.doe@example.com").password("s3cure-pass")
                .firstName("John").lastName("Doe").build();
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("Account with email 'john.doe@example.com' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void should_returnBadRequest_when_registerPasswordTooShort() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("new.customer@example.com").password("short")
                .firstName("New").lastName("Customer").build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    void should_returnUnauthorized_when_meCalledAnonymously() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(customerId = 1L)
    void should_returnCurrentUser_when_meCalledAuthenticated() throws Exception {
        // Arrange
        when(authService.me()).thenReturn(CurrentUserResponse.builder()
                .userId(2L).email("john.doe@example.com").role(Role.ROLE_CUSTOMER)
                .customerId(1L).firstName("John").lastName("Doe").build());

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.customerId").value(1));
    }
}
