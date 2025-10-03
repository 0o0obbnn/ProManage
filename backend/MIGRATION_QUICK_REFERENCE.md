# Database Migrations Quick Reference

## Default Credentials

### Admin User
```
Username: admin
Password: admin123
Email:    admin@promanage.com
```

**⚠️ IMPORTANT**: Change password immediately after first login!

## Database Schema Summary

### Tables Created (9)

#### Authentication & Authorization
1. `tb_user` - User accounts (id, username, password, email, real_name, avatar, status)
2. `tb_role` - System roles (7 default roles)
3. `tb_permission` - Permissions (47 permissions)
4. `tb_user_role` - User-role mapping
5. `tb_role_permission` - Role-permission mapping

#### Project Management
6. `tb_project` - Projects (id, name, code, description, status, owner_id)
7. `tb_project_member` - Project members

#### Document Management
8. `tb_document` - Documents (id, title, content, type, status, project_id)
9. `tb_document_version` - Document versions

### Default Roles (7)

| Role Code | Role Name | Description | Permissions |
|-----------|-----------|-------------|-------------|
| ROLE_SUPER_ADMIN | 超级管理员 | Full system access | 47 (all) |
| ROLE_PROJECT_MANAGER | 项目经理 | Project management | 18 |
| ROLE_DEVELOPER | 开发人员 | Development | 12 |
| ROLE_TESTER | 测试人员 | Testing | 12 |
| ROLE_DESIGNER | UI设计师 | Design | 12 |
| ROLE_OPS | 运维人员 | Operations | 11 |
| ROLE_EXTERNAL | 第三方人员 | Read-only | 9 |

### Permission Categories (47 total)

- **User Management**: 8 permissions (list, view, create, update, delete, reset_password, assign_role)
- **Role Management**: 6 permissions (list, view, create, update, delete, assign_permission)
- **Permission Management**: 5 permissions (list, view, create, update, delete)
- **Project Management**: 10 permissions (list, view, create, update, delete, archive, add_member, remove_member, view_members)
- **Document Management**: 10 permissions (list, view, create, update, delete, publish, archive, view_versions, create_version)
- **System Management**: 4 permissions (view_logs, monitor, clear_cache, config)
- **Profile Management**: 4 permissions (view, update, change_password, upload_avatar)

## Flyway Commands

### Check Migration Status
```bash
cd backend
mvn flyway:info
```

### Execute Pending Migrations
```bash
mvn flyway:migrate
```

### Validate Migrations
```bash
mvn flyway:validate
```

### Repair Metadata (Development Only)
```bash
mvn flyway:repair
```

## Database Verification Queries

### Check All Tables
```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

### Check Admin User
```sql
SELECT id, username, email, real_name, status
FROM tb_user
WHERE username = 'admin';
```

### Check Roles
```sql
SELECT id, role_name, role_code, description
FROM tb_role
ORDER BY sort;
```

### Check Permissions Count
```sql
SELECT COUNT(*) as total_permissions
FROM tb_permission
WHERE deleted = FALSE;
```

### Check Super Admin Permissions
```sql
SELECT COUNT(*) as super_admin_permissions
FROM tb_role_permission rp
JOIN tb_role r ON rp.role_id = r.id
WHERE r.role_code = 'ROLE_SUPER_ADMIN';
```

### Check Flyway History
```sql
SELECT installed_rank, version, description, type, script,
       installed_on, execution_time, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

### Check All Indexes
```sql
SELECT tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

## File Locations

```
backend/
├── pom.xml                                          # Flyway version: 10.12.0
├── promanage-service/
│   ├── pom.xml                                      # Flyway dependencies
│   └── src/main/resources/
│       └── db/migration/
│           ├── README.md                            # Full documentation
│           ├── V1.0.0__init_schema.sql             # Schema (442 lines)
│           └── V1.0.1__insert_initial_data.sql     # Data (286 lines)
└── promanage-api/
    └── src/main/resources/
        ├── application.yml
        ├── application-dev.yml                      # Flyway config
        └── application-prod.yml                     # Flyway config
```

## Statistics

- **Total SQL Lines**: 728
- **Tables Created**: 9 business tables + 1 Flyway metadata table
- **Indexes Created**: 30+
- **Foreign Keys**: 12
- **Triggers**: 7 (auto-update triggers)
- **Default Users**: 1 (admin)
- **Default Roles**: 7
- **Default Permissions**: 47
- **Demo Projects**: 1
- **Demo Documents**: 3

## Testing Checklist

### Prerequisites
- [ ] PostgreSQL 14+ running
- [ ] Database `promanage` created
- [ ] Redis running

### First Run
- [ ] `mvn clean install`
- [ ] Start ProManageApplication
- [ ] Check logs: "Successfully applied 2 migrations"
- [ ] Verify no errors

### Database Verification
- [ ] 10 tables exist
- [ ] Admin user exists
- [ ] 7 roles exist
- [ ] 47 permissions exist
- [ ] Flyway history has 2 entries

### API Testing
- [ ] Swagger UI accessible: http://localhost:8080/swagger-ui.html
- [ ] Login with admin/admin123
- [ ] JWT token returned
- [ ] Protected endpoints work with token

## Troubleshooting

### Issue: "Table already exists"
**Cause**: Migration already applied
**Solution**: Migrations are idempotent, this is expected on second run

### Issue: "Checksum mismatch"
**Cause**: Migration file was modified after execution
**Solution (Dev)**: `mvn flyway:repair` or `mvn flyway:clean && mvn flyway:migrate`
**Solution (Prod)**: Create new migration, never modify applied ones

### Issue: "Connection refused"
**Cause**: PostgreSQL not running
**Solution**: Start PostgreSQL service

### Issue: "Database does not exist"
**Cause**: Database not created
**Solution**:
```sql
CREATE DATABASE promanage;
```

## Migration Version Planning

- **V1.0.0**: ✅ Initial schema
- **V1.0.1**: ✅ Initial data
- **V1.0.2**: 🔜 Task tables
- **V1.0.3**: 🔜 Comment tables
- **V1.0.4**: 🔜 Attachment tables
- **V1.0.5**: 🔜 Notification tables
- **V1.1.0**: 🔜 Full-text search
- **V1.2.0**: 🔜 Performance indexes

## Next Steps

1. **Start Application** - Verify migrations execute successfully
2. **Test Authentication** - Login with admin credentials
3. **Test APIs** - Use Swagger UI to test all endpoints
4. **Run Tests** - Execute integration tests
5. **Security Review** - Change default passwords

## Support

For issues or questions, refer to:
- `db/migration/README.md` - Comprehensive guide
- `DATABASE_MIGRATIONS_SUMMARY.md` - Detailed implementation report
- Flyway Documentation: https://flywaydb.org/documentation/