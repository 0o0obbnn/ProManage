## Code Review Summary

**Project**: ProManage Backend
**Date**: 2025-10-12

This initial report covers **Phase 1 (Foundation & Configuration)** and **Phase 2 (Core Domain & Data Access)** of the planned code audit. The review was conducted against the project's extensive documentation.

### Overall Assessment: **Needs Improvement**

The backend is built on a modern, robust technology stack (Spring Boot 3, Java 21) and exhibits strong architectural design patterns, such as the use of a `BaseEntity`, a multi-module structure, and comprehensive configuration. However, critical flaws have been identified in the implementation of core security and data integrity features, which significantly increase risk and require immediate attention.

### Strengths

- **Modern Architecture**: The project correctly uses a multi-module Maven structure that enforces a clean separation of concerns (api, service, infrastructure, common).
- **Robust Foundation**: The use of a `BaseEntity` to centralize audit fields (`createdAt`, `creatorId`) and an optimistic locking `@Version` field is excellent practice.
- **Comprehensive Configuration**: The `application.yml` files are well-organized, using Spring profiles and externalized configuration for maintainability.
- **Good Test Practices**: The inclusion of Testcontainers in the `promanage-api` module indicates a commitment to reliable integration testing.
- **Rich Documentation**: Entities and DTOs are well-annotated with `@Schema`, leading to high-quality API documentation.

---

### Critical Issues

#### 1. Broken Logical Delete Implementation

- **Location**: `BaseEntity.java`, `application.yml`
- **Problem**: The project's logical delete mechanism is fundamentally broken. The `deletedAt` timestamp field in `BaseEntity.java` is **missing the `@TableLogic` annotation**. 
- **Impact**: This single omission causes the entire `mybatis-plus.global-config.db-config` section in `application.yml` to be ignored by the framework. Consequently, MyBatis-Plus does not automatically filter for `deleted_at IS NULL` on `SELECT` queries, nor does it convert `DELETE` operations into `UPDATE` statements. The system is relying on manual, error-prone queries and updates for a core data integrity feature, which will inevitably lead to data leakage (deleted data appearing in API responses) and failed deletions.
- **Recommended Fix**: 
  1. Add the `@TableLogic` annotation to the `deletedAt` field in `BaseEntity.java`.
  2. Ensure the `application.yml` values are correct for a timestamp strategy (they appear to be).
  3. Remove the manual compatibility methods (`getDeleted`, `setDeleted`) and refactor any code that uses them to rely on the standard MyBatis-Plus `deleteById()` or `delete()` methods.

```java
// In G:\nifa\ProManage\backend\promanage-common\src\main\java\com\promanage\common\entity\BaseEntity.java

import com.baomidou.mybatisplus.annotation.TableLogic; // <-- IMPORT THIS

// ... other annotations
public abstract class BaseEntity implements Serializable {

    // ... other fields

    /**
     * 逻辑删除时间
     */
    @TableLogic // <-- ADD THIS ANNOTATION
    @TableField(value = "deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "删除时间", example = "2025-09-30 11:00:00")
    private LocalDateTime deletedAt;

    // ...

    // REMOVE THESE MANUAL METHODS
    /*
    public boolean getDeleted() {
        return deletedAt != null;
    }

    public boolean isDeleted() {
        return getDeleted();
    }

    public void setDeleted(boolean deleted) {
        if (deleted) {
            if (this.deletedAt == null) {
                this.deletedAt = LocalDateTime.now();
            }
        } else {
            this.deletedAt = null;
        }
    }
    */
}
```

#### 2. Insecure Default JWT Secret

- **Location**: `application.yml`
- **Problem**: The JWT secret key defaults to a weak, predictable string: `your-secret-key-change-in-production`.
- **Impact**: If the `JWT_SECRET` environment variable is not set in a production environment, the application will launch with this known, insecure key. This would allow an attacker to easily forge JWT tokens, granting them unauthorized access to the entire system.
- **Recommended Fix**: Remove the default value or replace it with a mechanism that fails on startup if the secret isn't provided. Alternatively, programmatically generate a random key if one isn't set (though this has implications for distributed systems).

```yaml
# In G:\nifa\ProManage\backend\promanage-api\src\main\resources\application.yml

promanage:
  security:
    jwt:
      # CRITICAL: This default value MUST be removed or changed before production.
      # It is better to fail on startup if the environment variable is not set.
      secret: ${JWT_SECRET:your-secret-key-change-in-production} # <-- REMOVE THE DEFAULT
      # Should be: secret: ${JWT_SECRET}
```

---

### Important Issues

#### 1. Contradictory CORS Configuration

- **Location**: `SecurityConfig.java` and `application.yml`
- **Problem**: The CORS allowed origins are hardcoded within the `corsConfigurationSource` bean in `SecurityConfig.java`, completely ignoring the `promanage.security.cors.allowed-origins` property defined in `application.yml`.
- **Impact**: This makes it impossible to configure CORS for different environments (dev, staging, prod) without modifying and rebuilding the Java code. It violates the principle of externalized configuration and reduces operational flexibility.
- **Recommended Fix**: Inject the `promanage.security.cors.allowed-origins` property into `SecurityConfig` and use it to configure the `CorsConfiguration` bean.

```java
// In G:\nifa\ProManage\backend\promanage-infrastructure\src\main\java\com\promanage\infrastructure\security\SecurityConfig.java

// 1. Inject the property from application.yml
@Value("${promanage.security.cors.allowed-origins}")
private String[] allowedOrigins;

// 2. Use the injected property in the CORS configuration bean
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    // ...
    // Replace the hardcoded list with the injected property
    configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
    // ...
}
```

---

### Recommended Actions

1.  **Fix Logical Delete (Highest Priority)**: Immediately implement the recommended fix for the `@TableLogic` annotation. This is critical for data integrity.
2.  **Secure JWT Secret**: Remove the default JWT secret from `application.yml` to prevent accidental deployment with a known key.
3.  **Refactor CORS Configuration**: Modify `SecurityConfig.java` to use the properties from `application.yml` to make the application properly configurable.

Once these foundational issues are addressed, I will proceed with **Phase 3: Business Logic & Service Implementation Audit**.