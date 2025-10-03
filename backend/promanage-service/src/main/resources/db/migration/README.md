# Database Migrations

This directory contains Flyway database migration scripts for the ProManage system.

## Migration Files

### V1.0.0__init_schema.sql
**Purpose**: Initial database schema creation

This migration creates all core tables for the ProManage system:

#### User Management Tables
- `tb_user` - User accounts and profiles
- `tb_role` - System roles
- `tb_permission` - System permissions
- `tb_user_role` - User-role mapping
- `tb_role_permission` - Role-permission mapping

#### Project Management Tables
- `tb_project` - Project information
- `tb_project_member` - Project team members

#### Document Management Tables
- `tb_document` - Document metadata and content
- `tb_document_version` - Document version history

#### Features
- Complete table definitions with appropriate data types
- Column comments for documentation
- Comprehensive indexes for query optimization
- Foreign key constraints for referential integrity
- Automatic update_time triggers
- Logical deletion support

### V1.0.1__insert_initial_data.sql
**Purpose**: Insert initial system data

This migration populates the database with essential seed data:

#### Default Admin User
- **Username**: admin
- **Password**: admin123
- **Email**: admin@promanage.com
- **Role**: Super Admin

#### Default Roles
1. **超级管理员** (ROLE_SUPER_ADMIN) - Full system access
2. **项目经理** (ROLE_PROJECT_MANAGER) - Project management
3. **开发人员** (ROLE_DEVELOPER) - Development tasks
4. **测试人员** (ROLE_TESTER) - Testing tasks
5. **UI设计师** (ROLE_DESIGNER) - Design tasks
6. **运维人员** (ROLE_OPS) - Operations and monitoring
7. **第三方人员** (ROLE_EXTERNAL) - Read-only access

#### Default Permissions
Complete permission set covering:
- User management (8 permissions)
- Role management (6 permissions)
- Permission management (5 permissions)
- Project management (10 permissions)
- Document management (10 permissions)
- System management (4 permissions)
- Profile management (4 permissions)

#### Demo Project
- Sample project with demo documents (PRD, API, Test documents)
- Demonstrates document versioning

## Flyway Configuration

### Development Environment (application-dev.yml)
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true
```

### Production Environment (application-prod.yml)
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    baseline-version: 0
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true
```

## Migration Naming Convention

Flyway migrations follow this naming pattern:
```
V{version}__{description}.sql
```

Examples:
- `V1.0.0__init_schema.sql` - Initial schema
- `V1.0.1__insert_initial_data.sql` - Initial data
- `V1.0.2__add_task_table.sql` - Future migration

### Version Guidelines
- Use semantic versioning: MAJOR.MINOR.PATCH
- MAJOR: Breaking schema changes
- MINOR: New tables or columns
- PATCH: Minor changes, indexes, or data updates

## Running Migrations

### Automatic Execution
Migrations run automatically when the application starts if:
- `spring.flyway.enabled=true` (default)
- Database is accessible
- Migration files are in `classpath:db/migration`

### Manual Execution (Using Flyway Maven Plugin)

#### 1. Add Flyway Maven Plugin to promanage-api/pom.xml
```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>${flyway.version}</version>
    <configuration>
        <url>jdbc:postgresql://localhost:5432/promanage</url>
        <user>postgres</user>
        <password>postgres</password>
        <locations>
            <location>classpath:db/migration</location>
        </locations>
    </configuration>
</plugin>
```

#### 2. Run Migration Commands
```bash
# Show migration status
mvn flyway:info

# Execute pending migrations
mvn flyway:migrate

# Validate applied migrations
mvn flyway:validate

# Show migration history
mvn flyway:info

# Repair migration metadata (use with caution)
mvn flyway:repair

# Clean database (DANGEROUS - only for development)
mvn flyway:clean
```

## Database Schema Overview

### Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   tb_user   │       │   tb_role   │       │tb_permission│
│             │       │             │       │             │
│  - id       │       │  - id       │       │  - id       │
│  - username │       │  - role_name│       │  - perm_name│
│  - password │       │  - role_code│       │  - perm_code│
│  - email    │       │  - status   │       │  - type     │
└──────┬──────┘       └──────┬──────┘       └──────┬──────┘
       │                     │                     │
       │      ┌──────────────┴──────────────┐      │
       │      │                             │      │
       └─────►│     tb_user_role            │◄─────┘
              │                             │
              │  - user_id  (FK)            │
              │  - role_id  (FK)            │
              └─────────────────────────────┘
                            │
              ┌─────────────┴─────────────┐
              │                           │
              │  tb_role_permission       │
              │                           │
              │  - role_id      (FK)      │
              │  - permission_id (FK)     │
              └───────────────────────────┘

┌─────────────┐       ┌──────────────────┐
│  tb_project │       │tb_project_member │
│             │       │                  │
│  - id       │◄──────┤  - project_id    │
│  - name     │       │  - user_id       │
│  - owner_id ├──────►│  - role_id       │
│  - status   │       │  - join_time     │
└──────┬──────┘       └──────────────────┘
       │
       │
       │              ┌──────────────────────┐
       └─────────────►│    tb_document       │
                      │                      │
                      │  - id                │
                      │  - project_id  (FK)  │
                      │  - title             │
                      │  - content           │
                      │  - type              │
                      │  - status            │
                      │  - current_version   │
                      └──────────┬───────────┘
                                 │
                                 │
                      ┌──────────▼───────────┐
                      │ tb_document_version  │
                      │                      │
                      │  - id                │
                      │  - document_id (FK)  │
                      │  - version           │
                      │  - content           │
                      │  - change_log        │
                      └──────────────────────┘
```

## Troubleshooting

### Migration Failed
1. Check database connectivity
2. Verify migration file syntax
3. Check Flyway metadata table: `flyway_schema_history`
4. Use `mvn flyway:repair` if needed

### Checksum Mismatch
If you get a checksum mismatch error:
1. **Never modify applied migration files** in production
2. For development: Use `mvn flyway:repair` or `mvn flyway:clean`
3. Create a new migration file for schema changes

### Baseline Existing Database
If you need to add Flyway to an existing database:
```bash
mvn flyway:baseline -Dflyway.baselineVersion=1.0.0
```

## Best Practices

### DO
✅ Use sequential version numbers
✅ Write idempotent migrations when possible
✅ Test migrations on development/staging first
✅ Keep migrations small and focused
✅ Add comments to complex SQL
✅ Use transactions (implicit in PostgreSQL)
✅ Backup before running migrations in production

### DON'T
❌ Modify migration files after they're applied
❌ Use `flyway:clean` in production
❌ Skip testing migrations
❌ Create migrations with breaking changes without planning
❌ Forget to version control migration files
❌ Use DROP statements without careful consideration

## Security Notes

### Password Hashing
- Admin password is BCrypt hashed
- Strength: 10 rounds (2^10 iterations)
- Algorithm: BCrypt with salt
- Change default password immediately in production

### Database Credentials
- Use environment variables in production
- Never commit credentials to version control
- Use strong passwords for database users
- Restrict database access by IP

## Migration History

| Version | Date | Description |
|---------|------|-------------|
| V1.0.0 | 2025-09-30 | Initial schema creation |
| V1.0.1 | 2025-09-30 | Insert initial seed data |

## Future Migrations

Future migrations should follow this structure:
- V1.0.2 - V1.0.9: Minor patches and data updates
- V1.1.0 - V1.9.0: New features (tables, columns)
- V2.0.0+: Major version changes (breaking changes)

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)