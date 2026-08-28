package com.marom.ecommerce.api.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.marom.ecommerce.api.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Renders 403s raised inside the security filter chain (URL rule denies an authenticated
 * caller) in the same {@link ErrorResponse} shape as {@code GlobalExceptionHandler}.
 * Handles Spring Security's {@link AccessDeniedException}, not the application's
 * same-named {@code com.marom.ecommerce.api.exception.AccessDeniedException}. Builds its
 * own Jackson 3 mapper for the same bean-ordering reason as
 * {@link RestAuthenticationEntryPoint}.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message("Access is denied")
                .path(request.getRequestURI())
                .build();
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
