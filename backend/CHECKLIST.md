# ProManage Backend - Quick Start Checklist

## ✅ Pre-Development Checklist

### Environment Setup
- [ ] Install JDK 17 or higher
- [ ] Install Maven 3.9+
- [ ] Install PostgreSQL 15+
- [ ] Install Redis 7+
- [ ] Install IntelliJ IDEA (Community or Ultimate)
- [ ] Install Postman (for API testing)

### Database Setup
- [ ] Create PostgreSQL database: `createdb promanage`
- [ ] Verify connection: `psql -d promanage -U postgres`
- [ ] Start Redis: `redis-server`
- [ ] Verify Redis: `redis-cli ping` (should return PONG)

### Project Setup
- [ ] Open IntelliJ IDEA
- [ ] Import Maven project: G:\nifa\ProManage\backend
- [ ] Wait for Maven dependencies to download
- [ ] Verify JDK 17 is selected in Project Structure
- [ ] Install Lombok plugin in IntelliJ
- [ ] Enable annotation processing in IntelliJ

---

## 📋 Development Checklist

### Day 1: Infrastructure Module (4-6 hours)

#### Create Module Structure
- [ ] Create `promanage-infrastructure/pom.xml`
- [ ] Add infrastructure module to parent POM modules section
- [ ] Create package: `com.promanage.infrastructure.config`
- [ ] Create package: `com.promanage.infrastructure.security`
- [ ] Create package: `com.promanage.infrastructure.cache`

#### Security Configuration (Priority: HIGHEST)
- [ ] Create `SecurityConfig.java` - Spring Security configuration
- [ ] Create `JwtTokenProvider.java` - JWT token generation and validation
- [ ] Create `JwtAuthenticationFilter.java` - JWT request filter
- [ ] Create `SecurityUtils.java` - Security helper methods
- [ ] Create `PasswordEncoderConfig.java` - BCrypt configuration

#### Database Configuration
- [ ] Create `MyBatisPlusConfig.java` - Pagination, logical delete
- [ ] Create `MetaObjectHandler.java` - Auto-fill createTime, updateTime
- [ ] Create `DataSourceConfig.java` - Druid connection pool

#### Cache Configuration
- [ ] Create `RedisConfig.java` - Redis template configuration
- [ ] Create `CacheConfig.java` - Cache manager
- [ ] Create `CacheService.java` - Cache service interface

#### Test Infrastructure Module
- [ ] Build infrastructure module: `mvn clean install`
- [ ] Verify no compilation errors
- [ ] Write basic unit tests for utilities

---

### Day 2: Service Module - Entities (4-5 hours)

#### Create Module Structure
- [ ] Create `promanage-service/pom.xml`
- [ ] Add service module to parent POM
- [ ] Add dependency on common and infrastructure modules
- [ ] Create package: `com.promanage.service.entity`
- [ ] Create package: `com.promanage.service.mapper`
- [ ] Create package: `com.promanage.service.service`
- [ ] Create package: `com.promanage.service.impl`
- [ ] Create package: `com.promanage.service.converter`

#### Core Entities (Extend BaseEntity)
- [ ] Create `User.java` - User entity
  - [ ] Add fields: username, password, email, phone, avatar, status
  - [ ] Add @TableName("tb_user")
  - [ ] Add proper JavaDoc
  - [ ] Add @Schema annotations for Swagger

- [ ] Create `Role.java` - Role entity
  - [ ] Add fields: roleName, roleCode, description, sort
  - [ ] Add @TableName("tb_role")

- [ ] Create `Permission.java` - Permission entity
  - [ ] Add fields: permissionName, permissionCode, type, parentId
  - [ ] Add @TableName("tb_permission")

- [ ] Create `UserRole.java` - User-Role mapping
  - [ ] Add fields: userId, roleId
  - [ ] Add @TableName("tb_user_role")

- [ ] Create `RolePermission.java` - Role-Permission mapping
  - [ ] Add fields: roleId, permissionId
  - [ ] Add @TableName("tb_role_permission")

- [ ] Create `Project.java` - Project entity
  - [ ] Add fields: name, description, status, ownerId, startDate, endDate
  - [ ] Add @TableName("tb_project")

- [ ] Create `ProjectMember.java` - Project member mapping
  - [ ] Add fields: projectId, userId, roleId
  - [ ] Add @TableName("tb_project_member")

- [ ] Create `Document.java` - Document entity
  - [ ] Add fields: title, content, type, status, projectId, fileUrl, currentVersion
  - [ ] Add @TableName("tb_document")

- [ ] Create `DocumentVersion.java` - Document version
  - [ ] Add fields: documentId, version, content, changeLog
  - [ ] Add @TableName("tb_document_version")

#### Build and Verify
- [ ] Build service module: `mvn clean install`
- [ ] Verify all entities compile successfully

---

### Day 3: Service Module - Mappers & Services (4-5 hours)

#### Create Mappers (Extend BaseMapper<T>)
- [ ] Create `UserMapper.java`
  - [ ] Add custom query: findByUsername
  - [ ] Add custom query: findByEmail

- [ ] Create `RoleMapper.java`
  - [ ] Add custom query: findByUserId

- [ ] Create `PermissionMapper.java`
  - [ ] Add custom query: findByRoleId

- [ ] Create `ProjectMapper.java`
  - [ ] Add custom query: findByOwnerId
  - [ ] Add custom query: findByMemberId

- [ ] Create `DocumentMapper.java`
  - [ ] Add custom query: findByProjectId
  - [ ] Add custom query: searchByKeyword

- [ ] Create `ProjectMemberMapper.java`
- [ ] Create `DocumentVersionMapper.java`

#### Create Service Interfaces
- [ ] Create `IUserService.java`
  - [ ] Add method: getById
  - [ ] Add method: getByUsername
  - [ ] Add method: create
  - [ ] Add method: update
  - [ ] Add method: delete
  - [ ] Add method: listUsers (with pagination)

- [ ] Create `IRoleService.java`
- [ ] Create `IPermissionService.java`
- [ ] Create `IProjectService.java`
- [ ] Create `IDocumentService.java`

#### Create Service Implementations
- [ ] Create `UserServiceImpl.java`
  - [ ] Implement all interface methods
  - [ ] Add @Service annotation
  - [ ] Add @Transactional for modifications
  - [ ] Add proper logging
  - [ ] Add proper exception handling

- [ ] Create `RoleServiceImpl.java`
- [ ] Create `PermissionServiceImpl.java`
- [ ] Create `ProjectServiceImpl.java`
- [ ] Create `DocumentServiceImpl.java`

#### Create Object Converters (Using MapStruct)
- [ ] Create `UserConverter.java`
  - [ ] Add: toEntity(CreateUserRequest)
  - [ ] Add: toResponse(User)

- [ ] Create `DocumentConverter.java`
- [ ] Create `ProjectConverter.java`

#### Test Service Layer
- [ ] Write unit tests for UserServiceImpl
- [ ] Write unit tests for DocumentServiceImpl
- [ ] Verify 80%+ code coverage

---

### Day 4: API Module & Configuration (4-6 hours)

#### Create API Module Structure
- [ ] Create `promanage-api/pom.xml`
- [ ] Add api module to parent POM
- [ ] Add dependencies on all other modules
- [ ] Create package: `com.promanage.api.controller`
- [ ] Create package: `com.promanage.api.dto.request`
- [ ] Create package: `com.promanage.api.dto.response`

#### Create Main Application Class
- [ ] Create `ProManageApplication.java`
  - [ ] Add @SpringBootApplication
  - [ ] Add @MapperScan("com.promanage.service.mapper")
  - [ ] Add main method
  - [ ] Add SpringApplication.run()

#### Create DTOs

**Request DTOs**:
- [ ] Create `LoginRequest.java`
  - [ ] Add: username (required)
  - [ ] Add: password (required)
  - [ ] Add validation annotations (@NotBlank)

- [ ] Create `RegisterRequest.java`
- [ ] Create `CreateUserRequest.java`
- [ ] Create `UpdateUserRequest.java`
- [ ] Create `CreateProjectRequest.java`
- [ ] Create `CreateDocumentRequest.java`
- [ ] Create `UpdateDocumentRequest.java`

**Response DTOs**:
- [ ] Create `LoginResponse.java`
  - [ ] Add: token
  - [ ] Add: refreshToken
  - [ ] Add: userInfo

- [ ] Create `UserResponse.java`
- [ ] Create `ProjectResponse.java`
- [ ] Create `DocumentResponse.java`

#### Create Controllers

- [ ] Create `AuthController.java`
  - [ ] POST /api/auth/login - User login
  - [ ] POST /api/auth/register - User registration
  - [ ] POST /api/auth/logout - User logout
  - [ ] POST /api/auth/refresh - Refresh token
  - [ ] GET /api/auth/current - Get current user
  - [ ] Add @RestController, @RequestMapping
  - [ ] Add @Tag for Swagger
  - [ ] Add @Operation for each endpoint
  - [ ] Add @Valid for request validation

- [ ] Create `UserController.java`
  - [ ] GET /api/users - List users
  - [ ] GET /api/users/{id} - Get user
  - [ ] POST /api/users - Create user
  - [ ] PUT /api/users/{id} - Update user
  - [ ] DELETE /api/users/{id} - Delete user

- [ ] Create `ProjectController.java`
  - [ ] Implement CRUD operations
  - [ ] Add member management endpoints

- [ ] Create `DocumentController.java`
  - [ ] Implement CRUD operations
  - [ ] Add file upload endpoint
  - [ ] Add publish endpoint

#### Create Configuration Files

- [ ] Create `src/main/resources/application.yml`
```yaml
spring:
  application:
    name: promanage
  profiles:
    active: dev
```

- [ ] Create `src/main/resources/application-dev.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/promanage
    username: postgres
    password: your_password
  redis:
    host: localhost
    port: 6379
  flyway:
    enabled: true
    locations: classpath:db/migration

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: true
      logic-not-delete-value: false

jwt:
  secret: your-secret-key-change-in-production-min-256-bits
  expiration: 604800  # 7 days

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha

server:
  port: 8080
```

- [ ] Create `src/main/resources/application-prod.yml`
- [ ] Create `src/main/resources/logback-spring.xml`

#### Build API Module
- [ ] Build: `mvn clean install`
- [ ] Verify no compilation errors

---

### Day 5: Database & Final Testing (3-4 hours)

#### Create Flyway Migrations
- [ ] Create directory: `promanage-service/src/main/resources/db/migration`
- [ ] Create `V1.0.0__init_schema.sql`
  - [ ] Copy from ProManage_Database_Schema.sql
  - [ ] Create all tables
  - [ ] Add primary keys and foreign keys
  - [ ] Add indexes

- [ ] Create `V1.0.1__insert_initial_data.sql`
  - [ ] Insert super admin user (password: BCrypt hashed)
  - [ ] Insert default roles (SuperAdmin, ProjectManager, Developer, Tester, etc.)
  - [ ] Insert default permissions
  - [ ] Map permissions to roles

- [ ] Create `V1.0.2__create_indexes.sql`
  - [ ] Add performance indexes on foreign keys
  - [ ] Add indexes on frequently queried fields

#### First Run
- [ ] Start PostgreSQL
- [ ] Start Redis
- [ ] Run ProManageApplication
- [ ] Check startup logs for errors
- [ ] Verify Flyway migrations ran successfully
- [ ] Check database tables are created

#### API Testing
- [ ] Access Swagger UI: http://localhost:8080/swagger-ui.html
- [ ] Test health endpoint: http://localhost:8080/actuator/health
- [ ] Test login with admin user
- [ ] Verify JWT token is returned
- [ ] Test protected endpoint with token
- [ ] Test invalid token returns 401
- [ ] Test user CRUD operations
- [ ] Test document CRUD operations

#### Integration Testing
- [ ] Write AuthController integration test
- [ ] Write UserController integration test
- [ ] Write DocumentController integration test
- [ ] Run all tests: `mvn test`
- [ ] Verify all tests pass

---

## 🎯 Week 1 Success Criteria

### Must Have:
- [ ] Application starts without errors
- [ ] Database tables created automatically
- [ ] Can login with admin user
- [ ] JWT authentication working
- [ ] Swagger UI accessible
- [ ] User CRUD operations working
- [ ] Document CRUD operations working
- [ ] Global exception handling working
- [ ] Logging configured and working
- [ ] 80%+ test coverage

### Should Have:
- [ ] Redis caching working
- [ ] File upload working (basic)
- [ ] Permission checking working
- [ ] API documentation complete

### Nice to Have:
- [ ] Elasticsearch configured
- [ ] RabbitMQ configured
- [ ] Docker compose setup
- [ ] CI/CD pipeline

---

## 🐛 Common Issues Checklist

### Build Issues
- [ ] Verify JDK 17 is installed: `java -version`
- [ ] Verify Maven is installed: `mvn -version`
- [ ] Clean Maven cache: `mvn clean`
- [ ] Re-import Maven project in IntelliJ
- [ ] Check for dependency conflicts

### Database Issues
- [ ] PostgreSQL is running
- [ ] Database "promanage" exists
- [ ] Username and password are correct in application-dev.yml
- [ ] Flyway migrations have no syntax errors
- [ ] Check Flyway history: `SELECT * FROM flyway_schema_history;`

### Redis Issues
- [ ] Redis is running: `redis-cli ping`
- [ ] Redis host and port are correct
- [ ] No firewall blocking Redis port

### Application Issues
- [ ] Port 8080 is not in use
- [ ] All required environment variables set
- [ ] Logs show actual error (check logs/promanage.log)
- [ ] All required beans are created

---

## 📊 Progress Tracking

### Current Status: 30% Complete

#### Completed:
- ✅ Project structure
- ✅ Parent POM
- ✅ Common module (90%)
- ✅ Documentation
- ✅ .gitignore

#### In Progress:
- ⏳ Infrastructure module
- ⏳ Service module
- ⏳ API module

#### Not Started:
- ❌ Database migrations
- ❌ Unit tests
- ❌ Integration tests
- ❌ Docker setup

---

## 🎓 Learning Resources Checklist

### Must Read:
- [ ] ProManage_prd.md (Product requirements)
- [ ] ProManage_engineering_spec.md (Coding standards)
- [ ] PROJECT_IMPLEMENTATION_GUIDE.md (Implementation details)
- [ ] GETTING_STARTED.md (Quick start guide)

### Should Read:
- [ ] Spring Boot documentation
- [ ] MyBatis Plus documentation
- [ ] Spring Security with JWT tutorial
- [ ] PostgreSQL documentation

### Reference When Needed:
- [ ] ProManage_Database_Schema.sql
- [ ] ProManage_API_Specification.yaml
- [ ] Java best practices guide

---

## 🎉 Milestone Celebrations

### Celebrate When:
- [ ] First successful build
- [ ] Application starts for the first time
- [ ] First successful login
- [ ] First protected endpoint works with JWT
- [ ] First unit test passes
- [ ] 80% test coverage achieved
- [ ] Week 1 goals complete
- [ ] MVP complete

---

## 📝 Daily Standup Template

### What I Did Today:
- Created/completed: _____
- Fixed bugs: _____
- Wrote tests for: _____

### What I'm Doing Tomorrow:
- Plan to complete: _____
- Need to research: _____

### Blockers:
- Stuck on: _____
- Need help with: _____

---

**Remember**: Quality over speed! Take time to:
- Write clean, maintainable code
- Add comprehensive comments
- Write proper tests
- Follow the engineering spec
- Ask questions when stuck

**You've got this! 🚀**

---

Last Updated: 2025-09-30
Status: Ready for Development
Next Milestone: Complete Infrastructure Module