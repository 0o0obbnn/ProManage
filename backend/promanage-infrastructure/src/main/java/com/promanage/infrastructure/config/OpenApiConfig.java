package com.promanage.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * OpenAPI (Swagger) Configuration
 * <p>
 * Configures SpringDoc OpenAPI for API documentation and testing.
 * Provides interactive API documentation with JWT authentication support.
 * </p>
 * <p>
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access API docs at: http://localhost:8080/v3/api-docs
 * </p>
 *
 * @author ProManage Team
 * @since 2025-09-30
 */
@Slf4j
@Configuration
public class OpenApiConfig {

    /**
     * Application name
     */
    @Value("${spring.application.name:ProManage}")
    private String applicationName;

    /**
     * Application version
     */
    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    /**
     * Application description
     */
    @Value("${spring.application.description:ProManage Project Management System}")
    private String applicationDescription;

    /**
     * Server URL
     */
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * JWT security scheme name
     */
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    /**
     * Configure OpenAPI with general information and security
     *
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        log.info("Configuring OpenAPI documentation");

        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title(applicationName + " API Documentation")
                        .version(applicationVersion)
                        .description(applicationDescription + "\n\n" +
                                "## Features\n" +
                                "- Document Management\n" +
                                "- Change Management\n" +
                                "- Workspace Management\n" +
                                "- User and Permission Management\n" +
                                "- Real-time Notifications\n\n" +
                                "## Authentication\n" +
                                "This API uses JWT Bearer token authentication. " +
                                "To authenticate, obtain a token from the `/api/auth/login` endpoint " +
                                "and include it in the `Authorization` header as `Bearer <token>`.")
                        .contact(new Contact()
                                .name("ProManage Team")
                                .email("support@promanage.com")
                                .url("https://www.promanage.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                )
                // Server Information
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api-dev.promanage.com" + contextPath)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.promanage.com" + contextPath)
                                .description("Production Server")
                ))
                // Security Configuration
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from login endpoint")
                        )
                )
                // Apply security globally
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Configure API group for Authentication endpoints
     *
     * @return GroupedOpenApi for authentication
     */
    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("01-Authentication")
                .displayName("Authentication API")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Configure API group for User Management endpoints
     *
     * @return GroupedOpenApi for user management
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("02-Users")
                .displayName("User Management API")
                .pathsToMatch("/api/users/**")
                .build();
    }

    /**
     * Configure API group for Workspace Management endpoints
     *
     * @return GroupedOpenApi for workspace management
     */
    @Bean
    public GroupedOpenApi workspaceApi() {
        return GroupedOpenApi.builder()
                .group("03-Workspaces")
                .displayName("Workspace Management API")
                .pathsToMatch("/api/workspaces/**")
                .build();
    }

    /**
     * Configure API group for Project Management endpoints
     *
     * @return GroupedOpenApi for project management
     */
    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("04-Projects")
                .displayName("Project Management API")
                .pathsToMatch("/api/projects/**")
                .build();
    }

    /**
     * Configure API group for Document Management endpoints
     *
     * @return GroupedOpenApi for document management
     */
    @Bean
    public GroupedOpenApi documentApi() {
        return GroupedOpenApi.builder()
                .group("05-Documents")
                .displayName("Document Management API")
                .pathsToMatch("/api/documents/**")
                .build();
    }

    /**
     * Configure API group for Change Management endpoints
     *
     * @return GroupedOpenApi for change management
     */
    @Bean
    public GroupedOpenApi changeApi() {
        return GroupedOpenApi.builder()
                .group("06-Changes")
                .displayName("Change Management API")
                .pathsToMatch("/api/changes/**")
                .build();
    }

    /**
     * Configure API group for Activity and Notification endpoints
     *
     * @return GroupedOpenApi for activities
     */
    @Bean
    public GroupedOpenApi activityApi() {
        return GroupedOpenApi.builder()
                .group("07-Activities")
                .displayName("Activity & Notification API")
                .pathsToMatch("/api/activities/**", "/api/notifications/**")
                .build();
    }

    /**
     * Configure API group for Permission Management endpoints
     *
     * @return GroupedOpenApi for permissions
     */
    @Bean
    public GroupedOpenApi permissionApi() {
        return GroupedOpenApi.builder()
                .group("08-Permissions")
                .displayName("Permission Management API")
                .pathsToMatch("/api/permissions/**", "/api/roles/**")
                .build();
    }

    /**
     * Configure API group for File Management endpoints
     *
     * @return GroupedOpenApi for file management
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("09-Files")
                .displayName("File Management API")
                .pathsToMatch("/api/files/**")
                .build();
    }

    /**
     * Configure API group for Search endpoints
     *
     * @return GroupedOpenApi for search
     */
    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
                .group("10-Search")
                .displayName("Search API")
                .pathsToMatch("/api/search/**")
                .build();
    }

    /**
     * Configure API group for Admin endpoints
     *
     * @return GroupedOpenApi for admin
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("11-Admin")
                .displayName("Admin API")
                .pathsToMatch("/api/admin/**", "/api/system/**")
                .build();
    }

    /**
     * Configure API group for all endpoints
     *
     * @return GroupedOpenApi for all APIs
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("00-All-APIs")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * Note: Swagger UI Configuration in application.yml
     * <p>
     * Example configuration:
     * <pre>
     * springdoc:
     *   api-docs:
     *     path: /v3/api-docs
     *     enabled: true
     *   swagger-ui:
     *     path: /swagger-ui.html
     *     enabled: true
     *     tags-sorter: alpha
     *     operations-sorter: alpha
     *     doc-expansion: none
     *     display-request-duration: true
     *     default-models-expand-depth: 1
     *     default-model-expand-depth: 1
     *   show-actuator: false
     *   group-configs:
     *     - group: all
     *       display-name: All APIs
     *       paths-to-match: /api/**
     * </pre>
     * </p>
     */
}