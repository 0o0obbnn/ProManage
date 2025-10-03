package com.promanage.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point
 * <p>
 * Handles authentication failures by sending a JSON response with error details.
 * This is invoked when an unauthenticated user tries to access a protected resource.
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Handle authentication error
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param authException Authentication exception
     * @throws IOException if I/O error occurs
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.error("Authentication error: {} from IP: {}",
                authException.getMessage(),
                getClientIpAddress(request));

        // Set response status and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Build error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Authentication failed: " + authException.getMessage());
        errorResponse.put("path", request.getRequestURI());

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }

    /**
     * Get client IP address from request
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}