package com.marom.ecommerce.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Registers an HTTP bearer / JWT scheme so Swagger UI's "Authorize" button lets you paste
 * a token from {@code POST /api/v1/auth/login} and call the protected endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearer-jwt";

    @Bean
    OpenAPI ecommerceOpenApi() {
        return new OpenAPI()
                .info(new Info().title("ecommerce-api").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SCHEME_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
