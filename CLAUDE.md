# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Overview

ProManage is an intelligent project management system that provides unified knowledge base, intelligent change management, and test case management. The system aims to improve team collaboration efficiency by 50%, reduce rework by 30%, and achieve 70%+ test case reuse rate.

**Tech Stack:**
- **Backend**: Java 17 + Spring Boot 3.2.5 + PostgreSQL 15+ + MyBatis Plus
- **Frontend**: Vue 3 + TypeScript + Vite + Ant Design Vue (planned)
- **Infrastructure**: Redis (cache), Elasticsearch (search), RabbitMQ (messaging), MinIO (storage)

---

## Project Structure

```
ProManage/
├── backend/                           # Backend services (Spring Boot)
│   ├── promanage-common/             # Common module (Result, exceptions, utils)
│   ├── promanage-infrastructure/     # Infrastructure (config, security, cache)
│   ├── promanage-service/            # Business services (entities, mappers, services)
│   └── promanage-api/                # API layer (controllers, DTOs)
├── ProManage_prd.md                  # Product requirements (PRD)
├── ProManage_System_Architecture.md  # System architecture design
├── ProManage_engineering_spec.md     # Engineering standards
├── ProManage_Database_Schema.sql     # PostgreSQL schema
├── ProManage_API_Specification.yaml  # OpenAPI specification
└── ProManage_UIUX_Design_Part*.md   # UI/UX design documents
```

---

## Development Commands

### Backend (Maven)

```bash
# Navigate to backend directory
cd backend

# Build entire project
mvn clean install

# Build skipping tests
mvn clean install -DskipTests

# Run application (development)
cd promanage-api
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Run single test class
mvn test -Dtest=UserServiceTest

# Run single test method
mvn test -Dtest=UserServiceTest#testCreateUser

# Generate test coverage report
mvn clean test jacoco:report
# View report at: target/site/jacoco/index.html

# Code quality check
mvn checkstyle:check

# Run integration tests
mvn verify
```

### Database Setup

```bash
# Create database
createdb promanage

# Initialize schema
psql -d promanage -f ProManage_Database_Schema.sql

# Or use the detailed schema
psql -d promanage -f database_schema.sql
```

### API Documentation

After starting the backend:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

---

## Architecture Overview

### Microservices Design

The system is designed with 8 core microservices:

1. **User Service**: Authentication, authorization, user management
2. **Project Service**: Project lifecycle management
3. **Document Service**: Document storage, versioning, collaboration
4. **Change Service**: Change request workflow, impact analysis
5. **Task Service**: Task management, kanban board
6. **Test Service**: Test case management, reuse library
7. **Notification Service**: Multi-channel notifications
8. **Search Service**: Full-text search with Elasticsearch

### Data Flow

```
Client → API Gateway → Microservices → Data Layer
                     ↓                    ↓
              Authentication        PostgreSQL/Redis/
              Authorization         Elasticsearch
```

### Security Architecture

- **Authentication**: JWT-based stateless authentication
- **Authorization**: RBAC with 7 user roles (SuperAdmin, ProjectManager, Developer, Tester, UIDesigner, DevOps, Guest)
- **Data Isolation**: Multi-tenant with project-level data isolation
- **API Security**: All endpoints protected except public auth endpoints

---

## Key Design Patterns

### Backend Patterns

1. **Layered Architecture**: Controller → Service → Mapper → Database
2. **DTO Pattern**: Separate request/response DTOs from entities
3. **Result Wrapper**: Unified API response format with `Result<T>`
4. **Global Exception Handling**: `@ControllerAdvice` for consistent error responses
5. **Pagination**: Standard `PageResult<T>` with page/pageSize/total

### Database Patterns

1. **Soft Delete**: Use `deleted_at` timestamp instead of hard delete
2. **Audit Fields**: All tables have `created_at`, `updated_at`, `created_by`, `updated_by`
3. **Optimistic Locking**: Use `version` field for concurrent updates
4. **Multi-tenancy**: `organization_id` for tenant isolation

---

## Performance Requirements

- **API Response Time**: P95 ≤ 300ms
- **Page Load Time**: < 3 seconds
- **Search Response**: < 2 seconds
- **System Availability**: 99.9%+
- **Concurrent Users**: Support 500+

---

## Testing Standards

- **Unit Test Coverage**: ≥ 80%
- **Core Business Logic Coverage**: 100%
- **Integration Tests**: All controllers must have integration tests
- **Test Naming**: `should{ExpectedBehavior}_when{Condition}`

Example:
```java
@Test
void shouldReturnUser_whenValidIdProvided() {
    // given
    Long userId = 1L;

    // when
    User user = userService.getUserById(userId);

    // then
    assertNotNull(user);
    assertEquals(userId, user.getId());
}
```

---

## API Design Standards

### RESTful Conventions

- **GET**: Retrieve resources
- **POST**: Create resources
- **PUT**: Full update
- **PATCH**: Partial update
- **DELETE**: Remove resources

### URL Structure

```
/api/{version}/{resource}[/{id}][/{sub-resource}]

Examples:
GET    /api/v1/projects              # List projects
GET    /api/v1/projects/{id}         # Get project
POST   /api/v1/projects              # Create project
PUT    /api/v1/projects/{id}         # Update project
DELETE /api/v1/projects/{id}         # Delete project
GET    /api/v1/projects/{id}/members # Get project members
```

### Response Format

Success:
```json
{
  "code": 200,
  "message": "Success",
  "data": {...},
  "timestamp": 1727654400000
}
```

Error:
```json
{
  "code": 400,
  "message": "Invalid parameter",
  "timestamp": 1727654400000
}
```

Pagination:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "list": [...],
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": 1727654400000
}
```

---

## User Roles & Permissions

The system supports 7 distinct user roles with differentiated UIs:

1. **SuperAdmin**: System management, full access
2. **ProjectManager**: Project coordination, resource management, approvals
3. **Developer**: Task execution, document access (target: 20% efficiency boost)
4. **Tester**: Test case management, 70%+ reuse rate target
5. **UIDesigner**: Design file management, review cycle reduction (50% target)
6. **DevOps**: Deployment documentation, 15% success rate improvement target
7. **Guest**: Read-only access, zero data breach target

Each role has:
- Customized dashboard with role-specific metrics
- Theme color identifier
- Personalized workspace layout
- Permission-based UI variations

---

## Important Configuration Files

### Backend Configuration

- `backend/promanage-api/src/main/resources/application.yml`: Main config
- `backend/promanage-api/src/main/resources/application-dev.yml`: Dev environment
- `backend/promanage-api/src/main/resources/application-prod.yml`: Production
- `backend/pom.xml`: Maven dependencies

### Database Scripts

- `ProManage_Database_Schema.sql`: Complete PostgreSQL schema (25+ tables)
- `database_schema.sql`: Alternative schema with detailed comments
- `ProManage_Elasticsearch_Schema.json`: Elasticsearch mappings

### Documentation

- `ProManage_prd.md`: Product requirements and KPIs
- `ProManage_System_Architecture.md`: Architecture design
- `ProManage_engineering_spec.md`: Coding standards and conventions
- `ProManage_Technical_Design_Complete.md`: Complete technical design
- `ProManage_UIUX_Design_Part*.md`: UI/UX design specifications (6 parts)

---

## Common Development Workflows

### Adding a New API Endpoint

1. Define entity in `promanage-service/entity/`
2. Create mapper in `promanage-service/mapper/`
3. Implement service in `promanage-service/service/impl/`
4. Add controller in `promanage-api/controller/`
5. Create DTOs in `promanage-api/dto/`
6. Add Swagger annotations (`@Operation`, `@ApiResponse`)
7. Write unit tests for service layer
8. Write integration tests for controller

### Database Schema Changes

1. Update SQL in `ProManage_Database_Schema.sql`
2. Create migration script if using Flyway/Liquibase
3. Update corresponding entity classes
4. Regenerate MyBatis mappers if needed
5. Update relevant services
6. Update API documentation

---

## Code Quality Standards

| Metric | Requirement |
|--------|-------------|
| Code Coverage | ≥ 80% |
| Duplicate Code | ≤ 3% |
| Code Complexity | ≤ 10 |
| Critical Bugs | 0 |
| Sonar Quality Gate | Pass |

---

## Git Commit Convention

Follow Conventional Commits:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Example:
```
feat(document): implement document upload feature

- Support drag and drop upload
- Support multiple file upload
- Add progress indicator

Closes #123
```

---

## Environment Variables

Required environment variables for backend:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/promanage
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your_secret_key
JWT_EXPIRATION=86400000

# File Storage (MinIO)
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Elasticsearch
ES_HOST=localhost
ES_PORT=9200
```

---

## Troubleshooting

### Common Issues

1. **Database connection fails**: Check PostgreSQL is running and credentials in `application-dev.yml`
2. **Redis connection fails**: Ensure Redis is running on port 6379
3. **Tests fail**: Run `mvn clean test` to regenerate test classes
4. **Port 8080 already in use**: Change port in `application.yml` or stop conflicting service

### Debug Mode

Enable debug logging:
```yaml
logging:
  level:
    com.promanage: DEBUG
    org.springframework.web: DEBUG
```

---

## Performance Optimization Guidelines

1. **Database Queries**:
   - Use pagination for list queries
   - Add indexes on frequently queried columns
   - Avoid N+1 queries (use JOIN or batch queries)

2. **Caching Strategy**:
   - Static data: 1 hour cache
   - Dynamic data: 5 minutes cache
   - Real-time data: No cache or 1 minute

3. **API Response**:
   - Use DTO to return only necessary fields
   - Implement field filtering for large objects
   - Use compression for large responses

---

## Deployment Notes

### Docker Deployment

```bash
# Build image
docker build -t promanage-backend:latest .

# Run container
docker run -d \
  --name promanage-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  promanage-backend:latest
```

### Production Checklist

- [ ] Update `application-prod.yml` with production credentials
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS properly
- [ ] Set up database backups
- [ ] Configure monitoring (Prometheus/Grafana)
- [ ] Set up log aggregation
- [ ] Enable rate limiting
- [ ] Configure JWT secret to strong value
- [ ] Review and harden security settings

---

## Related Resources

- **Swagger API Docs**: http://localhost:8080/swagger-ui.html (when running)
- **Actuator Endpoints**: http://localhost:8080/actuator
- **PRD Document**: `ProManage_prd.md`
- **Architecture Doc**: `ProManage_System_Architecture.md`
- **Engineering Standards**: `ProManage_engineering_spec.md`