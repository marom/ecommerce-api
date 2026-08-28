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
import com.marom.ecommerce.api.dto.CustomerRequest;
import com.marom.ecommerce.api.dto.CustomerResponse;
import com.marom.ecommerce.api.exception.DuplicateResourceException;
import com.marom.ecommerce.api.exception.ResourceNotFoundException;
import com.marom.ecommerce.api.service.CustomerService;
import com.marom.ecommerce.api.support.SecurityTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(SecurityTestSupport.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WithMockUser(roles = "ADMIN")
class CustomerControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerService customerService;

    CustomerControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private CustomerRequest validCustomerRequest() {
        return CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("+1-555-0100")
                .address("123 Main St, Springfield")
                .build();
    }

    private CustomerResponse sampleCustomerResponse() {
        return CustomerResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phone("+1-555-0100")
                .address("123 Main St, Springfield")
                .build();
    }

    // ----- POST /api/v1/customers -----

    @Test
    void should_returnCreated_when_customerRequestIsValid() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        CustomerResponse response = sampleCustomerResponse();
        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    void should_returnBadRequest_when_firstNameIsBlank() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setFirstName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_firstNameExceedsMaxSize() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setFirstName("a".repeat(101));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_lastNameIsBlank() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setLastName("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_lastNameExceedsMaxSize() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setLastName("a".repeat(101));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_emailIsBlank() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setEmail("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_emailIsNotWellFormed() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setEmail("not-an-email");

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_emailExceedsMaxSize() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setEmail("a".repeat(250) + "@a.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_phoneExceedsMaxSize() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setPhone("1".repeat(21));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnConflict_when_emailAlreadyInUseOnCreate() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        when(customerService.createCustomer(any(CustomerRequest.class)))
                .thenThrow(new DuplicateResourceException("Customer with email 'jane.doe@example.com' already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ----- GET /api/v1/customers/{id} -----

    @Test
    void should_returnOk_when_customerExists() throws Exception {
        // Arrange
        when(customerService.getCustomer(1L)).thenReturn(sampleCustomerResponse());

        // Act & Assert
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    void should_returnNotFound_when_customerDoesNotExistOnGet() throws Exception {
        // Arrange
        when(customerService.getCustomer(404L)).thenThrow(new ResourceNotFoundException("Customer not found with id 404"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customers/404"))
                .andExpect(status().isNotFound());
    }

    // ----- GET /api/v1/customers -----

    @Test
    void should_returnOk_when_listingAllCustomers() throws Exception {
        // Arrange
        when(customerService.getAllCustomers()).thenReturn(List.of(sampleCustomerResponse()));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("jane.doe@example.com"));
    }

    // ----- PUT /api/v1/customers/{id} -----

    @Test
    void should_returnOk_when_customerUpdateIsValid() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setFirstName("Janet");
        CustomerResponse response = sampleCustomerResponse();
        response.setFirstName("Janet");
        when(customerService.updateCustomer(eq(1L), any(CustomerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/customers/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Janet"));
    }

    @Test
    void should_returnBadRequest_when_firstNameIsBlankOnUpdate() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setFirstName("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/customers/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnBadRequest_when_emailIsNotWellFormedOnUpdate() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        request.setEmail("not-an-email");

        // Act & Assert
        mockMvc.perform(put("/api/v1/customers/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_returnNotFound_when_customerDoesNotExistOnUpdate() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        when(customerService.updateCustomer(eq(404L), any(CustomerRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 404"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/customers/404").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnConflict_when_emailAlreadyInUseOnUpdate() throws Exception {
        // Arrange
        CustomerRequest request = validCustomerRequest();
        when(customerService.updateCustomer(eq(1L), any(CustomerRequest.class)))
                .thenThrow(new DuplicateResourceException("Customer with email 'jane.doe@example.com' already exists"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/customers/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ----- DELETE /api/v1/customers/{id} -----

    @Test
    void should_returnNoContent_when_customerIsDeleted() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_returnNotFound_when_customerDoesNotExistOnDelete() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Customer not found with id 404"))
                .when(customerService).deleteCustomer(404L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/customers/404"))
                .andExpect(status().isNotFound());
    }

    // ----- security -----

    @Test
    @WithAnonymousUser
    void should_return401_when_noAuthOnList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void should_return403_when_customerRoleOnList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isForbidden());
    }
}
