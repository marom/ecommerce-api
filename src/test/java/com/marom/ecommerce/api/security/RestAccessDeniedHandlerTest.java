package com.marom.ecommerce.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RestAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RestAccessDeniedHandler handler = new RestAccessDeniedHandler();

    @Test
    void should_write403ErrorResponseBody_when_handleInvoked() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        handler.handle(request, response, new AccessDeniedException("denied"));

        // Assert
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json");
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("error").asText()).isEqualTo("Forbidden");
        assertThat(body.get("path").asText()).isEqualTo("/api/v1/products");
        assertThat(body.hasNonNull("timestamp")).isTrue();
    }
}
