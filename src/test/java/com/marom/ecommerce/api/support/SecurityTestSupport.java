package com.marom.ecommerce.api.support;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.marom.ecommerce.api.config.CorsConfig;
import com.marom.ecommerce.api.config.OpenApiConfig;
import com.marom.ecommerce.api.config.SecurityConfig;
import com.marom.ecommerce.api.security.AppUserDetailsService;
import com.marom.ecommerce.api.security.CurrentUserService;
import com.marom.ecommerce.api.security.JwtRolesConverter;
import com.marom.ecommerce.api.security.JwtService;
import com.marom.ecommerce.api.security.RestAccessDeniedHandler;
import com.marom.ecommerce.api.security.RestAuthenticationEntryPoint;

/**
 * Bundles the real security wiring for {@code @WebMvcTest} slices. Import it with
 * {@code @Import(SecurityTestSupport.class)}. Authentication in slice tests is injected via
 * {@code spring-security-test} ({@code @WithMockUser} / {@link WithMockCustomUser}), so the
 * {@code AppUserDetailsService} the {@code AuthenticationManager} needs is a no-op mock.
 * The JWT secret comes from {@code src/test/resources/application.properties}.
 */
@TestConfiguration
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        OpenApiConfig.class,
        JwtRolesConverter.class,
        JwtService.class,
        CurrentUserService.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class
})
public class SecurityTestSupport {

    @Bean
    AppUserDetailsService appUserDetailsService() {
        return Mockito.mock(AppUserDetailsService.class);
    }
}
