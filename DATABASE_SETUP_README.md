# ProManage 数据库快速初始化

## 🚀 一键初始化（推荐）

```bash
# 最简单的方式 - 执行一个文件即可
psql -h 192.168.2.144 -U postgres -d promanage -f COMPLETE_DATABASE_INIT.sql
```

**完成后会看到**:
```
================================================================
ProManage 数据库初始化完成！
================================================================
默认管理员账户:
  用户名: admin
  密码: admin123
================================================================
```

---

## 📚 详细文档

- **完整指南**: 查看 [DATABASE_MIGRATION_GUIDE.md](DATABASE_MIGRATION_GUIDE.md)
- **清理报告**: 查看 [SQL_CLEANUP_SUMMARY.md](SQL_CLEANUP_SUMMARY.md)

---

## ✅ 验证安装

```sql
-- 检查表数量（应该是 20+ 张表）
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';

-- 查看用户
SELECT * FROM tb_user;
```

---

## 🔑 默认账户

- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: 超级管理员

---

## 📂 文件说明

| 文件 | 用途 | 何时使用 |
|------|------|----------|
| **COMPLETE_DATABASE_INIT.sql** | ⭐ 完整初始化脚本 | 首次安装/重置数据库 |
| **DATABASE_MIGRATION_GUIDE.md** | 📖 详细迁移指南 | 了解详细步骤和问题解决 |
| **SQL_CLEANUP_SUMMARY.md** | 📊 SQL清理报告 | 了解清理过程和修复内容 |
| `backend/promanage-service/src/main/resources/db/migration/V1.0.*` | Flyway迁移文件 | 生产环境增量更新 |

---

## ⚠️ 常见问题

**Q: 表已存在怎么办？**
```sql
-- 删除数据库重新创建
DROP DATABASE promanage;
CREATE DATABASE promanage;
-- 然后重新执行初始化脚本
```

**Q: 如何重置数据库？**
```bash
# 删除所有表
psql -h 192.168.2.144 -U postgres -d promanage -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
# 重新初始化
psql -h 192.168.2.144 -U postgres -d promanage -f COMPLETE_DATABASE_INIT.sql
```

---

## 🎉 完成

数据库初始化完成后，启动应用：

```bash
cd backend/promanage-api
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

访问: http://localhost:8080/swagger-ui.html
