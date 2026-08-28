package com.marom.ecommerce.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-context security smoke test. Requires the MySQL database seeded from
 * {@code db/schema.sql} (which now includes the {@code users} table and demo accounts).
 * Runs under Failsafe during {@code mvn verify}, not the fast {@code mvn test} suite.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SecurityEndToEndIT {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    SecurityEndToEndIT(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new LoginBody(email, password));
        String json = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("accessToken").asText();
    }

    private String productBody() throws Exception {
        return objectMapper.writeValueAsString(new ProductBody(
                "IT Product " + System.nanoTime(), "created by SecurityEndToEndIT",
                "9.99", "IT-" + System.nanoTime(), 5, true, 1L));
    }

    @Test
    void should_allowAdminToCreateProduct_when_bearerTokenIsPresent() throws Exception {
        String token = login("admin@shop.example.com", "admin123");

        mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(productBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void should_return401WithErrorResponseShape_when_noToken() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(productBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void should_return403_when_customerCallsAdminEndpoint() throws Exception {
        String token = login("john.doe@example.com", "password123");

        mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(productBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_allowPublicProductRead_when_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    void should_return401_when_accessingNonHealthActuatorEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isUnauthorized());
    }

    private record LoginBody(String email, String password) {
    }

    private record ProductBody(String name, String description, String price, String sku, int stockQuantity,
            boolean active, long categoryId) {
    }
}
