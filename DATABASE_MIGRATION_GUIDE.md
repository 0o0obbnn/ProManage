# ProManage 数据库迁移指南

## 📋 概述

本文档提供ProManage项目数据库的完整迁移和初始化指南。经过SQL清理和重构，现在有两种方式初始化数据库。

---

## 🔍 SQL文件清理总结

### ✅ 已完成的清理工作

1. **归档冲突文件**
   - ✅ 移动 `ProManage_Database_Schema.sql` → `docs/archive/old_schemas/`
   - ✅ 移动 `database_schema.sql` → `docs/archive/old_schemas/`
   - ✅ 删除 `add_version_column.sql` (冗余文件)

2. **修复损坏的迁移文件**
   - ✅ 归档 `004_create_project_activity_table.sql` (表名引用错误)
   - ✅ 归档 `006_add_test_case_and_permission_indexes.sql` (表不存在)
   - ✅ 创建 `V1.0.3__create_project_activity_table.sql` (修复版本)

3. **创建缺失的表迁移**
   - ✅ 创建 `V1.0.4__create_task_tables.sql` (任务、任务依赖、任务评论)
   - ✅ 创建 `V1.0.5__create_change_request_tables.sql` (变更请求、影响、审批)
   - ✅ 创建 `V1.0.6__create_test_case_tables.sql` (测试用例、执行历史)

---

## 🎯 数据库初始化方案

### 方案一：使用完整初始化脚本（推荐 - 最简单）

**适用场景**：首次部署、全新安装

#### 步骤：

1. **确保数据库已创建**
   ```sql
   CREATE DATABASE promanage;
   ```

2. **执行完整初始化脚本**
   ```bash
   # 使用psql命令行
   psql -h 192.168.2.144 -U postgres -d promanage -f COMPLETE_DATABASE_INIT.sql
   ```

3. **验证安装**
   ```sql
   -- 检查表数量
   SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';

   -- 应该看到约 20+ 张表
   ```

4. **登录测试**
   - 用户名: `admin`
   - 密码: `admin123`

---

### 方案二：使用Flyway迁移脚本（生产推荐）

**适用场景**：生产环境、版本控制、增量迁移

#### 执行顺序：

```bash
# 1. 基础设置
psql -h 192.168.2.144 -U postgres -d promanage -f database_setup.sql

# 2. 核心表结构
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.0__init_schema.sql

# 3. 初始数据
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.1__insert_initial_data.sql

# 4. 更新管理员密码
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.2__update_admin_password.sql

# 5. 项目活动表
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.3__create_project_activity_table.sql

# 6. 任务管理表
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.4__create_task_tables.sql

# 7. 变更请求表
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.5__create_change_request_tables.sql

# 8. 测试用例表
psql -h 192.168.2.144 -U postgres -d promanage -f backend/promanage-service/src/main/resources/db/migration/V1.0.6__create_test_case_tables.sql

# 9. 扩展迁移（可选）
psql -h 192.168.2.144 -U postgres -d promanage -f database_migrations/001_add_content_type_to_documents.sql
psql -h 192.168.2.144 -U postgres -d promanage -f database_migrations/002_create_document_folder_table.sql
psql -h 192.168.2.144 -U postgres -d promanage -f database_migrations/003_add_document_indexes.sql
psql -h 192.168.2.144 -U postgres -d promanage -f database_migrations/005_create_notification_table.sql
```

---

## 📊 数据库表结构

### MVP Phase 1 核心表（P0优先级）

| 表名 | 说明 | 状态 | 依赖 |
|------|------|------|------|
| **tb_user** | 用户表 | ✅ V1.0.0 | - |
| **tb_role** | 角色表 | ✅ V1.0.0 | - |
| **tb_permission** | 权限表 | ✅ V1.0.0 | - |
| **tb_user_role** | 用户角色关联 | ✅ V1.0.0 | tb_user, tb_role |
| **tb_role_permission** | 角色权限关联 | ✅ V1.0.0 | tb_role, tb_permission |
| **tb_project** | 项目表 | ✅ V1.0.0 | tb_user |
| **tb_project_member** | 项目成员 | ✅ V1.0.0 | tb_project, tb_user |
| **tb_document** | 文档表 | ✅ V1.0.0 | tb_project, tb_user |
| **tb_document_version** | 文档版本 | ✅ V1.0.0 | tb_document, tb_user |
| **tb_document_folder** | 文档文件夹 | ✅ 002_migration | tb_project |
| **tb_task** | 任务表 | ✅ V1.0.4 | tb_project, tb_user |
| **tb_task_dependency** | 任务依赖 | ✅ V1.0.4 | tb_task |
| **tb_task_comment** | 任务评论 | ✅ V1.0.4 | tb_task, tb_user |
| **tb_change_request** | 变更请求 | ✅ V1.0.5 | tb_project, tb_user |
| **tb_change_request_impact** | 变更影响 | ✅ V1.0.5 | tb_change_request |
| **tb_change_request_approval** | 变更审批 | ✅ V1.0.5 | tb_change_request, tb_user |
| **tb_test_case** | 测试用例 | ✅ V1.0.6 | tb_project, tb_user, tb_task |
| **tb_test_execution** | 测试执行历史 | ✅ V1.0.6 | tb_test_case, tb_user |
| **tb_project_activity** | 项目活动 | ✅ V1.0.3 | tb_project, tb_user |
| **tb_notification** | 通知表 | ✅ 005_migration | tb_user |

### 总计：20+ 核心表

---

## 🔑 默认账户

### 管理员账户
- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: 超级管理员
- **权限**: 所有权限

### 默认角色
1. **SUPER_ADMIN** (ID: 1) - 超级管理员
2. **NORMAL_USER** (ID: 2) - 普通用户
3. **PROJECT_MANAGER** (ID: 3) - 项目经理
4. **DEVELOPER** (ID: 4) - 开发人员
5. **TESTER** (ID: 5) - 测试人员

---

## 🔍 验证步骤

### 1. 检查表是否创建成功

```sql
-- 查看所有表
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- 应该看到类似输出:
-- tb_change_request
-- tb_change_request_approval
-- tb_change_request_impact
-- tb_document
-- tb_document_folder
-- tb_document_version
-- tb_notification
-- tb_permission
-- tb_project
-- tb_project_activity
-- tb_project_member
-- tb_role
-- tb_role_permission
-- tb_task
-- tb_task_comment
-- tb_task_dependency
-- tb_test_case
-- tb_test_execution
-- tb_user
-- tb_user_role
```

### 2. 检查初始数据

```sql
-- 检查用户
SELECT id, username, real_name FROM tb_user;

-- 检查角色
SELECT id, role_code, role_name FROM tb_role;

-- 检查权限数量
SELECT COUNT(*) as permission_count FROM tb_permission;
```

### 3. 测试登录

```bash
# 使用curl测试API
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 应该返回JWT token
```

---

## ⚠️ 常见问题

### 问题1: 表已存在错误

**现象**：
```
ERROR: relation "tb_user" already exists
```

**解决方案**：
```sql
-- 方案A: 删除现有数据库重新创建
DROP DATABASE promanage;
CREATE DATABASE promanage;
-- 然后重新执行初始化脚本

-- 方案B: 只删除特定表
DROP TABLE IF EXISTS tb_user CASCADE;
-- 然后重新执行对应迁移
```

### 问题2: 外键约束错误

**现象**：
```
ERROR: insert or update on table violates foreign key constraint
```

**解决方案**：
- 确保按照正确顺序执行迁移脚本
- V1.0.0 必须在所有其他迁移之前执行
- V1.0.1 必须在 V1.0.0 之后执行

### 问题3: Flyway校验失败

**现象**：
```
Flyway checksum mismatch
```

**解决方案**：
```sql
-- 清空Flyway历史表
DELETE FROM flyway_schema_history;

-- 或完全删除并重新初始化
DROP TABLE flyway_schema_history;
```

---

## 📁 文件组织结构

### ✅ 保留的SQL文件

```
ProManage/
├── COMPLETE_DATABASE_INIT.sql                    # 完整初始化脚本（推荐）
├── database_setup.sql                            # 基础设置
├── backend/promanage-service/src/main/resources/db/migration/
│   ├── V1.0.0__init_schema.sql                  # 核心表结构
│   ├── V1.0.1__insert_initial_data.sql          # 初始数据
│   ├── V1.0.2__update_admin_password.sql        # 更新密码
│   ├── V1.0.3__create_project_activity_table.sql # 项目活动表
│   ├── V1.0.4__create_task_tables.sql           # 任务相关表
│   ├── V1.0.5__create_change_request_tables.sql # 变更请求表
│   └── V1.0.6__create_test_case_tables.sql      # 测试用例表
├── database_migrations/
│   ├── 001_add_content_type_to_documents.sql    # 文档类型字段
│   ├── 002_create_document_folder_table.sql     # 文档文件夹
│   ├── 003_add_document_indexes.sql             # 文档索引
│   └── 005_create_notification_table.sql        # 通知表
└── backend/test-db-connection.sql               # 连接测试脚本
```

### 📦 已归档的文件

```
docs/archive/old_schemas/
├── ProManage_Database_Schema.sql                # 旧Schema A（organizations模式）
├── database_schema.sql                          # 旧Schema A副本
├── 004_create_project_activity_table.sql        # 损坏的迁移（表名错误）
└── 006_add_test_case_and_permission_indexes.sql # 损坏的迁移（表不存在）
```

---

## 🚀 生产环境部署建议

### 1. 使用Flyway自动迁移（推荐）

在 `application-prod.yml` 中配置:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    locations: classpath:db/migration
    validate-on-migrate: true
```

应用启动时会自动执行迁移。

### 2. 手动迁移

适用于需要严格控制的生产环境：

```bash
# 1. 备份现有数据库
pg_dump -h host -U user -d promanage > backup_$(date +%Y%m%d).sql

# 2. 执行迁移
psql -h host -U user -d promanage -f COMPLETE_DATABASE_INIT.sql

# 3. 验证
psql -h host -U user -d promanage -c "SELECT COUNT(*) FROM tb_user"
```

### 3. 数据库备份策略

```bash
# 每日自动备份
0 2 * * * pg_dump -h 192.168.2.144 -U postgres promanage | gzip > /backup/promanage_$(date +\%Y\%m\%d).sql.gz

# 保留最近30天
find /backup -name "promanage_*.sql.gz" -mtime +30 -delete
```

---

## 📈 性能优化建议

### 1. 关键索引已创建
- ✅ 用户名唯一索引
- ✅ 项目代码唯一索引
- ✅ 外键索引
- ✅ 状态字段索引
- ✅ 时间字段降序索引
- ✅ 文档标题全文索引（pg_trgm）

### 2. 建议的额外优化

```sql
-- 为频繁查询的字段添加部分索引
CREATE INDEX idx_task_active ON tb_task(status, priority)
WHERE deleted = FALSE AND status IN (0,1);

-- 为大表启用自动分析
ALTER TABLE tb_document SET (autovacuum_analyze_scale_factor = 0.05);

-- 为时间序列数据启用分区（可选，适用于大量数据）
-- 例如按月分区tb_project_activity表
```

---

## ✅ 检查清单

部署前请确认：

- [ ] PostgreSQL 15+ 已安装
- [ ] 数据库 `promanage` 已创建
- [ ] 已选择初始化方案（方案一或方案二）
- [ ] 按顺序执行所有迁移脚本
- [ ] 验证表数量（应为20+张表）
- [ ] 验证管理员账户可以登录
- [ ] 检查Flyway迁移历史表
- [ ] 执行性能索引创建
- [ ] 配置定期备份

---

## 📞 技术支持

如遇到数据库迁移问题，请查看：

1. **日志文件**: `logs/promanage-dev.log`
2. **SQL分析报告**: 本项目根目录下生成的分析报告
3. **归档文件**: `docs/archive/old_schemas/` 中的旧版本

---

## 🎉 完成

数据库迁移完成后，您可以：

1. 启动ProManage应用
2. 访问 http://localhost:8080/swagger-ui.html 测试API
3. 使用 `admin/admin123` 登录系统

**祝您使用愉快！** 🚀
