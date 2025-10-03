# ProManage Backend - Project Implementation Guide

## 📋 Project Overview

This document provides a comprehensive guide for the ProManage backend implementation, following the PRD and Engineering Specifications.

**Project**: ProManage - Project Management System
**Version**: 1.0.0-SNAPSHOT
**Tech Stack**: Spring Boot 3.2.5, Java 17, MyBatis Plus, PostgreSQL, Redis, Elasticsearch, RabbitMQ
**Working Directory**: `G:\nifa\ProManage\backend`

---

## ✅ What Has Been Created

### 1. Project Structure
```
backend/
├── pom.xml                          ✅ Created - Parent POM with all dependencies
├── promanage-common/                ✅ Module created
│   ├── pom.xml                      ✅ Created
│   └── src/main/java/com/promanage/common/
│       ├── domain/
│       │   ├── Result.java          ✅ Created - Unified response wrapper
│       │   ├── ResultCode.java      ✅ Created - Response code enum
│       │   └── PageResult.java      ✅ Created - Page result wrapper
│       ├── exception/
│       │   ├── BusinessException.java        ✅ Created
│       │   └── GlobalExceptionHandler.java   ✅ Created
│       └── constant/
│           └── SystemConstant.java  ✅ Created
├── promanage-infrastructure/        ⏳ To be created
├── promanage-service/               ⏳ To be created
└── promanage-api/                   ⏳ To be created
```

### 2. Core Components Created

#### Common Module (70% Complete)
- ✅ **Result**: Unified API response wrapper
- ✅ **ResultCode**: Comprehensive response codes (200, 4xx, 5xx, business codes)
- ✅ **PageResult**: Pagination result wrapper
- ✅ **BusinessException**: Business logic exception
- ✅ **GlobalExceptionHandler**: Global exception handling with proper logging
- ✅ **SystemConstant**: System-wide constants

#### Still Needed in Common Module
- ⏳ Utility classes (DateUtil, StringUtil, ValidationUtil, etc.)
- ⏳ Common enums (UserStatus, DocumentStatus, ProjectStatus, etc.)
- ⏳ Base entity classes
- ⏳ Validation annotations

---

## 📦 Next Steps - Implementation Order

### Phase 1: Core Infrastructure Setup (Priority: P0)

#### Step 1: Complete Common Module
Create these additional files:

**Utilities** (`common/util/`):
```java
- DateUtil.java          // Date/time utilities
- StringUtil.java        // String manipulation
- ValidationUtil.java    // Validation helpers
- EncryptUtil.java       // Encryption/decryption
- IdGenerator.java       // Snowflake ID generator
- JsonUtil.java          // JSON serialization
```

**Enums** (`common/enums/`):
```java
- UserStatus.java        // ACTIVE, DISABLED, LOCKED
- DocumentStatus.java    // DRAFT, PUBLISHED, ARCHIVED
- ProjectStatus.java     // ACTIVE, COMPLETED, ARCHIVED
- RoleType.java          // SUPER_ADMIN, PROJECT_MANAGER, DEVELOPER, etc.
- PermissionType.java    // READ, WRITE, DELETE, ADMIN
```

**Base Classes** (`common/domain/`):
```java
- BaseEntity.java        // Base entity with id, createTime, updateTime, deleted
```

#### Step 2: Create Infrastructure Module

**Directory Structure**:
```
promanage-infrastructure/
├── pom.xml
└── src/main/java/com/promanage/infrastructure/
    ├── config/
    │   ├── MyBatisPlusConfig.java
    │   ├── RedisConfig.java
    │   ├── ElasticsearchConfig.java
    │   ├── RabbitMQConfig.java
    │   ├── MinIOConfig.java
    │   └── WebMvcConfig.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtTokenProvider.java
    │   ├── JwtAuthenticationFilter.java
    │   └── PasswordEncoder.java
    ├── cache/
    │   ├── CacheService.java
    │   └── RedisKeyGenerator.java
    ├── mq/
    │   ├── RabbitMQProducer.java
    │   └── RabbitMQConsumer.java
    └── storage/
        ├── FileStorageService.java
        ├── MinIOStorageService.java
        └── S3StorageService.java
```

**Key Configuration Classes**:

1. **MyBatisPlusConfig.java**:
   - Configure pagination plugin
   - Configure logical delete
   - Configure auto-fill for timestamps
   - Configure SQL performance plugin (dev only)

2. **SecurityConfig.java**:
   - Configure JWT authentication
   - Configure CORS
   - Configure security filter chain
   - Configure password encryption (BCrypt)

3. **RedisConfig.java**:
   - Configure RedisTemplate
   - Configure cache manager
   - Configure serialization

#### Step 3: Create Service Module

**Directory Structure**:
```
promanage-service/
├── pom.xml
└── src/main/java/com/promanage/service/
    ├── entity/              // Database entities
    │   ├── User.java
    │   ├── Role.java
    │   ├── Permission.java
    │   ├── UserRole.java
    │   ├── RolePermission.java
    │   ├── Project.java
    │   ├── ProjectMember.java
    │   ├── Document.java
    │   ├── DocumentVersion.java
    │   └── ChangeRequest.java
    ├── mapper/              // MyBatis mappers
    │   ├── UserMapper.java
    │   ├── RoleMapper.java
    │   ├── PermissionMapper.java
    │   ├── ProjectMapper.java
    │   ├── DocumentMapper.java
    │   └── ChangeRequestMapper.java
    ├── service/             // Service interfaces
    │   ├── IUserService.java
    │   ├── IRoleService.java
    │   ├── IPermissionService.java
    │   ├── IProjectService.java
    │   ├── IDocumentService.java
    │   └── IChangeRequestService.java
    ├── impl/                // Service implementations
    │   ├── UserServiceImpl.java
    │   ├── RoleServiceImpl.java
    │   ├── PermissionServiceImpl.java
    │   ├── ProjectServiceImpl.java
    │   ├── DocumentServiceImpl.java
    │   └── ChangeRequestServiceImpl.java
    ├── converter/           // Object converters (MapStruct)
    │   ├── UserConverter.java
    │   ├── DocumentConverter.java
    │   └── ProjectConverter.java
    └── enums/               // Service-specific enums
```

**Key Entities to Create**:

1. **User.java** (tb_user):
   ```java
   - id (BIGINT, PK)
   - username (VARCHAR)
   - password (VARCHAR, encrypted)
   - email (VARCHAR)
   - phone (VARCHAR)
   - avatar (VARCHAR)
   - status (INTEGER) // UserStatus
   - deleted (BOOLEAN)
   - createTime (TIMESTAMP)
   - updateTime (TIMESTAMP)
   ```

2. **Document.java** (tb_document):
   ```java
   - id (BIGINT, PK)
   - title (VARCHAR)
   - content (TEXT)
   - type (VARCHAR)
   - status (INTEGER)
   - projectId (BIGINT, FK)
   - creatorId (BIGINT, FK)
   - currentVersion (VARCHAR)
   - fileUrl (VARCHAR)
   - deleted (BOOLEAN)
   - createTime (TIMESTAMP)
   - updateTime (TIMESTAMP)
   ```

3. **Project.java** (tb_project):
   ```java
   - id (BIGINT, PK)
   - name (VARCHAR)
   - description (TEXT)
   - status (INTEGER)
   - ownerId (BIGINT, FK)
   - startDate (DATE)
   - endDate (DATE)
   - deleted (BOOLEAN)
   - createTime (TIMESTAMP)
   - updateTime (TIMESTAMP)
   ```

#### Step 4: Create API Module

**Directory Structure**:
```
promanage-api/
├── pom.xml
└── src/main/java/com/promanage/api/
    ├── controller/
    │   ├── AuthController.java
    │   ├── UserController.java
    │   ├── RoleController.java
    │   ├── ProjectController.java
    │   ├── DocumentController.java
    │   └── ChangeRequestController.java
    ├── dto/
    │   ├── request/
    │   │   ├── LoginRequest.java
    │   │   ├── RegisterRequest.java
    │   │   ├── CreateUserRequest.java
    │   │   ├── UpdateUserRequest.java
    │   │   ├── CreateProjectRequest.java
    │   │   ├── CreateDocumentRequest.java
    │   │   └── UpdateDocumentRequest.java
    │   └── response/
    │       ├── LoginResponse.java
    │       ├── UserResponse.java
    │       ├── ProjectResponse.java
    │       └── DocumentResponse.java
    └── ProManageApplication.java  // Main application class
```

**Key Controllers**:

1. **AuthController.java**:
   - POST /api/auth/login - User login
   - POST /api/auth/register - User registration
   - POST /api/auth/logout - User logout
   - POST /api/auth/refresh - Refresh token
   - GET /api/auth/current - Get current user info

2. **DocumentController.java**:
   - GET /api/documents - List documents (with pagination)
   - GET /api/documents/{id} - Get document detail
   - POST /api/documents - Create document
   - PUT /api/documents/{id} - Update document
   - DELETE /api/documents/{id} - Delete document
   - GET /api/documents/{id}/versions - Get document versions
   - POST /api/documents/{id}/publish - Publish document

3. **ProjectController.java**:
   - GET /api/projects - List projects
   - GET /api/projects/{id} - Get project detail
   - POST /api/projects - Create project
   - PUT /api/projects/{id} - Update project
   - DELETE /api/projects/{id} - Delete project
   - GET /api/projects/{id}/members - Get project members

#### Step 5: Database Schema & Migrations

**Create Flyway Migrations** (`promanage-service/src/main/resources/db/migration/`):

```
V1.0.0__init_schema.sql              // Initial database schema
V1.0.1__insert_initial_data.sql      // Super admin and default roles
V1.0.2__create_indexes.sql           // Performance indexes
```

**V1.0.0__init_schema.sql** should include:
- tb_user (Users table)
- tb_role (Roles table)
- tb_permission (Permissions table)
- tb_user_role (User-Role mapping)
- tb_role_permission (Role-Permission mapping)
- tb_project (Projects table)
- tb_project_member (Project members)
- tb_document (Documents table)
- tb_document_version (Document versions)
- tb_change_request (Change requests table)

#### Step 6: Application Configuration

**Create Configuration Files** (`promanage-api/src/main/resources/`):

1. **application.yml** (base configuration)
2. **application-dev.yml** (development environment)
3. **application-prod.yml** (production environment)
4. **logback-spring.xml** (logging configuration)

**Example application.yml**:
```yaml
spring:
  application:
    name: promanage
  profiles:
    active: dev
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/promanage
    username: postgres
    password: password
  redis:
    host: localhost
    port: 6379
  rabbitmq:
    host: localhost
    port: 5672

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

jwt:
  secret: your-secret-key-change-in-production
  expiration: 604800  # 7 days in seconds
```

---

## 🗄️ Database Tables to Create

### Phase 1 MVP Tables (P0)

1. **tb_user** - User information
2. **tb_role** - Role information
3. **tb_permission** - Permission information
4. **tb_user_role** - User-Role mapping
5. **tb_role_permission** - Role-Permission mapping
6. **tb_project** - Project information
7. **tb_project_member** - Project member mapping
8. **tb_document** - Document information
9. **tb_document_version** - Document version history
10. **tb_change_request** - Change request tracking

### Reference the Existing SQL
You already have:
- `G:\nifa\ProManage\ProManage_Database_Schema.sql`
- `G:\nifa\ProManage\database_schema.sql`

Use these as reference for the complete table structure.

---

## 🔐 Security Implementation Checklist

### JWT Authentication
- [ ] JwtTokenProvider - Token generation and validation
- [ ] JwtAuthenticationFilter - JWT filter for requests
- [ ] SecurityConfig - Spring Security configuration
- [ ] Password encryption using BCrypt
- [ ] Token refresh mechanism

### RBAC Permission Control
- [ ] Role-based access control
- [ ] Permission checking annotations (@PreAuthorize)
- [ ] Project-level permission checking
- [ ] Data-level permission filtering

---

## 📝 API Documentation

### Swagger/OpenAPI Setup
- [ ] Configure SpringDoc OpenAPI
- [ ] Add API annotations to controllers
- [ ] Document all DTOs with @Schema
- [ ] Provide examples in annotations

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

---

## 🧪 Testing Strategy

### Unit Tests
- [ ] Service layer tests (80% coverage)
- [ ] Mapper layer tests
- [ ] Utility class tests (100% coverage)

### Integration Tests
- [ ] Controller integration tests
- [ ] Database integration tests
- [ ] Security integration tests

### Test Files Location
```
src/test/java/com/promanage/
├── service/
│   └── UserServiceTest.java
├── mapper/
│   └── UserMapperTest.java
└── controller/
    └── AuthControllerTest.java
```

---

## 🚀 Build and Run Instructions

### Prerequisites
- JDK 17+
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.12+ (optional for MVP)

### Database Setup
```bash
# Create database
createdb promanage

# Run the init script
psql -d promanage -f ProManage_Database_Schema.sql
```

### Build Project
```bash
cd G:\nifa\ProManage\backend
mvn clean install
```

### Run Application
```bash
cd promanage-api
mvn spring-boot:run
```

Or run the main class: `com.promanage.api.ProManageApplication`

### Access Points
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

---

## 📊 Development Priorities

### Week 1: Infrastructure (Current Week)
- [x] Create Maven multi-module structure
- [x] Create common module (Result, Exception, Constants)
- [ ] Create infrastructure module (Security, Config)
- [ ] Create database schema with Flyway
- [ ] Configure Spring Boot application

### Week 2: Core Authentication
- [ ] Implement User entity and mapper
- [ ] Implement Role and Permission entities
- [ ] Implement JWT authentication
- [ ] Implement login/logout/register APIs
- [ ] Implement RBAC permission checking

### Week 3: Document Management (MVP Core)
- [ ] Implement Document entity and mapper
- [ ] Implement Document CRUD service
- [ ] Implement Document controller
- [ ] Implement file upload/download
- [ ] Implement document version control

### Week 4: Project Management
- [ ] Implement Project entity and mapper
- [ ] Implement Project CRUD service
- [ ] Implement Project controller
- [ ] Implement project member management

### Week 5-6: Integration & Testing
- [ ] Write unit tests (80% coverage)
- [ ] Write integration tests
- [ ] API testing with Postman
- [ ] Performance testing
- [ ] Security testing

---

## 🔧 Recommended Development Tools

### IDEs
- **IntelliJ IDEA Ultimate** (Recommended)
  - Install Lombok plugin
  - Install MyBatisX plugin
  - Install Vue.js plugin (for frontend)

### Database Tools
- **DBeaver** or **DataGrip** for PostgreSQL management

### API Testing
- **Postman** for API testing
- **Swagger UI** for API documentation

### Version Control
- **Git** with proper .gitignore
- **GitKraken** or **SourceTree** (optional GUI)

---

## 📚 Additional Resources

### Documentation References
- Spring Boot 3: https://spring.io/projects/spring-boot
- MyBatis Plus: https://baomidou.com/
- PostgreSQL: https://www.postgresql.org/docs/
- SpringDoc OpenAPI: https://springdoc.org/

### Project Documents (Already Created)
- `G:\nifa\ProManage\ProManage_prd.md` - Product Requirements
- `G:\nifa\ProManage\ProManage_engineering_spec.md` - Engineering Standards
- `G:\nifa\ProManage\ProManage_Database_Schema.sql` - Database Schema
- `G:\nifa\ProManage\ProManage_API_Specification.yaml` - API Spec

---

## 🎯 Immediate Next Actions

### For You to Do Next:

1. **Complete Common Module** (30 minutes)
   - Create utility classes (DateUtil, StringUtil, etc.)
   - Create enums (UserStatus, DocumentStatus, etc.)
   - Create BaseEntity class

2. **Create Infrastructure Module** (1-2 hours)
   - Create MyBatisPlusConfig
   - Create SecurityConfig with JWT
   - Create RedisConfig

3. **Create Service Module** (2-3 hours)
   - Create User entity
   - Create Role and Permission entities
   - Create Document and Project entities
   - Create corresponding Mappers

4. **Create API Module** (1-2 hours)
   - Create ProManageApplication main class
   - Create AuthController with login/logout
   - Create basic DTOs (LoginRequest, UserResponse, etc.)

5. **Database Setup** (30 minutes)
   - Use existing SQL schema file
   - Create Flyway migrations
   - Initialize database with test data

6. **Run and Test** (30 minutes)
   - Build the project
   - Run the application
   - Test login API with Postman
   - Access Swagger UI

---

## 📞 Support

For questions or issues during implementation:
1. Review the engineering specification document
2. Check Spring Boot documentation
3. Review MyBatis Plus documentation
4. Check existing SQL schema files

---

**Document Version**: 1.0
**Last Updated**: 2025-09-30
**Status**: In Progress - Week 1

---

## Quick Reference Commands

```bash
# Build entire project
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run application
cd promanage-api && mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generate IDE files
mvn idea:idea  # For IntelliJ IDEA
mvn eclipse:eclipse  # For Eclipse
```

Good luck with the implementation! 🚀