-- =====================================================
-- ProManage 数据库执行顺序指南
-- =====================================================

## 执行顺序

按照以下顺序执行SQL文件，确保数据库正确初始化：

### 1. 基础设置
```sql
-- 执行数据库基础设置（角色、扩展等）
\i database_setup.sql
```

### 2. 核心表结构
```sql
-- 创建核心业务表（使用tb_前缀命名）
\i backend/promanage-service/src/main/resources/db/migration/V1.0.0__init_schema.sql
```

### 3. 初始数据
```sql
-- 插入系统初始数据
\i backend/promanage-service/src/main/resources/db/migration/V1.0.1__insert_initial_data.sql
```

### 4. 管理员密码更新
```sql
-- 更新管理员密码（如果需要）
\i backend/promanage-service/src/main/resources/db/migration/V1.0.2__update_admin_password.sql
```

### 5. 表结构增强
```sql
-- 添加版本列和其他增强字段
\i add_version_column.sql
```

### 6. 数据库迁移（按数字顺序执行）
```sql
-- 迁移文件1：添加文档内容类型
\i database_migrations/001_add_content_type_to_documents.sql

-- 迁移文件2：创建文档文件夹表
\i database_migrations/002_create_document_folder_table.sql

-- 迁移文件3：添加文档索引
\i database_migrations/003_add_document_indexes.sql

-- 迁移文件4：创建项目活动表
\i database_migrations/004_create_project_activity_table.sql

-- 迁移文件5：创建通知表
\i database_migrations/005_create_notification_table.sql

-- 迁移文件6：添加测试用例和权限索引
\i database_migrations/006_add_test_case_and_permission_indexes.sql
```

### 7. 测试连接（可选）
```sql
-- 验证数据库连接和表创建
\i backend/test-db-connection.sql
```

## 注意事项

1. **不要执行** `database_schema.sql` 和 `ProManage_Database_Schema.sql`
   - 这两个文件与项目实际使用的表结构不一致
   - 项目使用tb_前缀的表命名规范

2. **执行前检查**
   - 确保PostgreSQL版本为15+
   - 确保数据库已创建：`CREATE DATABASE promanage;`
   - 确保执行用户有足够权限

3. **常见错误解决**
   - 如果提示角色不存在，先执行`database_setup.sql`
   - 如果提示表已存在，检查是否重复执行了初始化脚本
   - 如果提示权限不足，使用数据库管理员账户执行

## 验证步骤

执行完成后，运行以下SQL验证：

```sql
-- 检查表是否创建成功
SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE 'tb_%';

-- 检查角色是否创建成功
SELECT rolname FROM pg_roles WHERE rolname IN ('application_user', 'readonly_user');

-- 检查初始数据
SELECT COUNT(*) FROM tb_user;
SELECT COUNT(*) FROM tb_role;
SELECT COUNT(*) FROM tb_permission;
```