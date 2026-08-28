package com.marom.ecommerce.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class RestAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint();

    @Test
    void should_write401ErrorResponseBody_when_commenceInvoked() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        // Assert
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json");
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("status").asInt()).isEqualTo(401);
        assertThat(body.get("error").asText()).isEqualTo("Unauthorized");
        assertThat(body.get("path").asText()).isEqualTo("/api/v1/orders");
        assertThat(body.hasNonNull("timestamp")).isTrue();
    }
}
