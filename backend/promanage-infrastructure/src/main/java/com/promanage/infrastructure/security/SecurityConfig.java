package com.promanage.infrastructure.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security Configuration
 *
 * <p>Configures Spring Security with JWT authentication, CORS, and authorization rules. This
 * configuration uses stateless session management suitable for REST APIs.
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private static final String ADMIN_ROLE = "ADMIN";

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final UserDetailsService userDetailsService; // Injected UserDetailsService

  @Value("${promanage.security.cors.allowed-origins}")
  private String[] allowedOrigins;

  /** Public endpoints that don't require authentication */
  private static final String[] PUBLIC_ENDPOINTS = {
    // Authentication endpoints
    "/api/auth/login",
    "/api/auth/register",
    "/api/auth/refresh-token",
    "/api/auth/forgot-password",
    "/api/auth/reset-password",

    // Authentication endpoints (v1)
    "/api/v1/auth/login",
    "/api/v1/auth/register",
    "/api/v1/auth/refresh",
    "/api/v1/auth/forgot-password",
    "/api/v1/auth/reset-password",

    // Public documentation
    "/api/public/**",

    // Health check and actuator
    "/actuator/health",
    "/actuator/info",

    // API Documentation (Swagger/OpenAPI)
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/v3/api-docs/**",
    "/swagger-resources/**",
    "/webjars/**",

    // Favicon
    "/favicon.ico",

    // Error page
    "/error"
  };

  /**
   * Configure HTTP security
   *
   * @param http HttpSecurity object
   * @return SecurityFilterChain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    log.info("Configuring Spring Security filter chain");

    http
        // Disable CSRF for stateless JWT authentication
        .csrf(AbstractHttpConfigurer::disable)

        // Configure CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // Configure exception handling
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

        // Configure session management (stateless for JWT)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure security headers
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.deny())  // 防止点击劫持
            .contentTypeOptions(contentTypeOptions -> {})  // 防止MIME类型嗅探
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                .maxAgeInSeconds(31_536_000)  // 1年
                .includeSubDomains(true)
                .preload(true))
            .addHeaderWriter((request, response) -> {
                // 添加CSP头部
                response.setHeader("Content-Security-Policy", 
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: https:; " +
                    "font-src 'self' data:; " +
                    "connect-src 'self'");
                
                // 添加其他安全头部
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
            }))

        // Configure authorization rules
        .authorizeHttpRequests(
            auth ->
                auth
                    // Public endpoints
                    .requestMatchers(PUBLIC_ENDPOINTS)
                    .permitAll()

                    // Specific HTTP method permissions
                    .requestMatchers(HttpMethod.GET, "/api/documents/public/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/projects/public/**")
                    .permitAll()

                    // Admin-only endpoints
                    .requestMatchers("/api/admin/**")
                    .hasRole(ADMIN_ROLE)
                    .requestMatchers("/api/system/**")
                    .hasRole(ADMIN_ROLE)

                    // Manager and Admin can access certain endpoints
                    .requestMatchers("/api/projects/*/settings/**")
                    .hasAnyRole(ADMIN_ROLE, "MANAGER")
                    .requestMatchers("/api/workspaces/*/settings/**")
                    .hasAnyRole(ADMIN_ROLE, "MANAGER")

                    // All other requests require authentication
                    .anyRequest()
                    .authenticated())

        // Add JWT authentication filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // Disable HTTP Basic authentication
        .httpBasic(AbstractHttpConfigurer::disable)

        // Disable form login
        .formLogin(AbstractHttpConfigurer::disable)

        // Disable logout (handled by client)
        .logout(AbstractHttpConfigurer::disable);

    log.info("Spring Security filter chain configured successfully");
    return http.build();
  }

  /**
   * Configure CORS
   *
   * @return CorsConfigurationSource
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    log.info("Configuring CORS");

    CorsConfiguration configuration = new CorsConfiguration();

    // Allow specific origins (configure in application.yml for production)
    configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));

    // Allow all HTTP methods
    configuration.setAllowedMethods(
        Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.HEAD.name()));

    // 限制允许的头部，提高安全性
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization", 
        "Content-Type", 
        "Accept", 
        "X-Requested-With",
        "Cache-Control",
        "X-Total-Count", 
        "X-Page-Number", 
        "X-Page-Size"
    ));

    // Expose headers
    configuration.setExposedHeaders(
        Arrays.asList(
            "Authorization", "Content-Type", "X-Total-Count", "X-Page-Number", "X-Page-Size"));

    // Allow credentials - 仅在必要时启用
    configuration.setAllowCredentials(true);

    // Cache CORS configuration for 1 hour
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    log.info("CORS configured successfully");
    return source;
  }

  /**
   * Configure AuthenticationManager bean
   *
   * @param authenticationConfiguration AuthenticationConfiguration
   * @return AuthenticationManager
   * @throws Exception if configuration fails
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  /**
   * Configure PasswordEncoder bean
   *
   * <p>Uses BCrypt for secure password hashing
   *
   * @return PasswordEncoder BCrypt password encoder
   */
  @Bean
  public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
    log.info("Initializing BCrypt password encoder");
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
  }

  /**
   * Configure AuthenticationProvider bean
   *
   * <p>Wires the UserDetailsService and PasswordEncoder together.
   *
   * @return AuthenticationProvider
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }
}
