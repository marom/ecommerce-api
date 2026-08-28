package com.marom.ecommerce.api.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marom.ecommerce.api.dto.ChangeRoleRequest;
import com.marom.ecommerce.api.dto.UserResponse;
import com.marom.ecommerce.api.entity.Role;
import com.marom.ecommerce.api.service.UserService;
import com.marom.ecommerce.api.support.SecurityTestSupport;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    UserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private UserResponse sampleUserResponse() {
        return UserResponse.builder().id(2L).email("john.doe@example.com").role(Role.ROLE_CUSTOMER)
                .customerId(1L).enabled(true).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnOk_when_adminListsUsers() throws Exception {
        // Arrange
        when(userService.listUsers()).thenReturn(List.of(sampleUserResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void should_returnForbidden_when_customerListsUsers() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void should_returnUnauthorized_when_anonymousListsUsers() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnOk_when_adminChangesRole() throws Exception {
        // Arrange
        ChangeRoleRequest request = ChangeRoleRequest.builder().role(Role.ROLE_ADMIN).build();
        UserResponse response = sampleUserResponse();
        response.setRole(Role.ROLE_ADMIN);
        when(userService.changeRole(eq(2L), eq(Role.ROLE_ADMIN))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/2/role").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_returnBadRequest_when_roleIsMissing() throws Exception {
        // Arrange
        ChangeRoleRequest request = ChangeRoleRequest.builder().build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/2/role").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
