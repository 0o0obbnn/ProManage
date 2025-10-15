package com.promanage.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // Injected UserDetailsService
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Authorization header name
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer token prefix
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Bearer prefix length
     */
    private static final int BEARER_PREFIX_LENGTH = 7;

    /**
     * Filter method to intercept requests and validate JWT tokens
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract JWT token from request
            String token = extractTokenFromRequest(request);

            // Validate token and set authentication
            if (StringUtils.isNotBlank(token)) {
                // Check if token is blacklisted first
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("Blacklisted JWT token detected from IP: {}", getClientIpAddress(request));
                    // Continue without authentication - blacklisted tokens should be rejected
                } else if (jwtTokenProvider.validateToken(token)) {
                    setAuthenticationFromToken(token, request);
                } else {
                    log.warn("Invalid JWT token detected from IP: {}", getClientIpAddress(request));
                }
            }
        } catch (Exception e) {
            log.error("Failed to set user authentication in security context", e);
            // Don't throw exception - let the request continue without authentication
            // The security configuration will handle unauthorized access
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * @param request HTTP request
     * @return JWT token string or null if not found
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }

        return null;
    }

    /**
     * Set authentication in SecurityContext from JWT token
     *
     * @param token JWT token
     * @param request HTTP request
     */
    private void setAuthenticationFromToken(String token, HttpServletRequest request) {
        // Extract username from token
        String username = jwtTokenProvider.getUsernameFromToken(token);

        if (StringUtils.isNotBlank(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Create authentication token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            // Set additional details
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Successfully authenticated user: {} from token", username);
        }
    }

    /**
     * Get client IP address from request
     * Handles proxy headers (X-Forwarded-For, X-Real-IP)
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For may contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Determine if the filter should be applied to the request
     * Override this method to skip filtering for certain paths
     *
     * @param request HTTP request
     * @return true if filter should not be applied
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // You can add logic here to skip filtering for certain paths
        // For example: return request.getRequestURI().startsWith("/public");
        return false;
    }
}