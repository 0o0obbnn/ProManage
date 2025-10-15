# 数据库迁移执行指南

## 📋 迁移概览

**版本**: V1.1.0  
**目标**: 组织模块重构 - 租户隔离、软删除优化、性能提升  
**预计停机时间**: 10-15分钟  
**影响表**: organizations, organization_audit_logs (新增)

---

## 🔍 迁移前检查

### 1. 环境验证
```bash
# 检查PostgreSQL版本(需要 ≥ 12.0)
psql --version

# 检查数据库连接
psql -h localhost -U promanage -d promanage_prod -c "SELECT version();"

# 检查当前schema版本
psql -h localhost -U promanage -d promanage_prod -c "
  SELECT version, description, installed_on 
  FROM flyway_schema_history 
  ORDER BY installed_rank DESC LIMIT 1;
"
```

### 2. 数据量评估
```sql
-- 检查organizations表数据量
SELECT 
    COUNT(*) as total_rows,
    COUNT(*) FILTER (WHERE deleted_at IS NULL) as active_rows,
    pg_size_pretty(pg_total_relation_size('organizations')) as table_size
FROM organizations;

-- 检查是否有大量待迁移数据
SELECT 
    COUNT(*) as rows_without_tenant_id
FROM organizations
WHERE tenant_id IS NULL;
```

### 3. 磁盘空间检查
```sql
-- 检查数据库大小
SELECT 
    pg_database.datname,
    pg_size_pretty(pg_database_size(pg_database.datname)) AS size
FROM pg_database
WHERE datname = 'promanage_prod';

-- 检查表空间
SELECT 
    tablespace_name,
    pg_size_pretty(pg_tablespace_size(tablespace_name)) AS size
FROM pg_tablespace;
```

**要求**: 至少有当前数据库大小的30%空闲空间

---

## 💾 数据备份

### 完整备份
```bash
# 创建备份目录
mkdir -p /backup/promanage/$(date +%Y%m%d)

# 全量备份
pg_dump -h localhost -U promanage -d promanage_prod \
  -F c -b -v \
  -f /backup/promanage/$(date +%Y%m%d)/promanage_full_$(date +%H%M%S).backup

# 验证备份文件
ls -lh /backup/promanage/$(date +%Y%m%d)/
```

### 表级备份(快速恢复)
```bash
# 仅备份organizations表
pg_dump -h localhost -U promanage -d promanage_prod \
  -t organizations \
  -F c -b -v \
  -f /backup/promanage/$(date +%Y%m%d)/organizations_$(date +%H%M%S).backup
```

### 数据导出(CSV格式)
```sql
-- 导出organizations表数据
COPY organizations TO '/backup/promanage/organizations_backup.csv' 
WITH (FORMAT CSV, HEADER true);
```

---

## 🚀 迁移执行

### 方式1: Flyway自动迁移(推荐)

#### 配置Flyway
```properties
# flyway-prod.conf
flyway.url=jdbc:postgresql://localhost:5432/promanage_prod
flyway.user=promanage
flyway.password=${PROMANAGE_DB_PASSWORD}
flyway.schemas=public
flyway.locations=filesystem:./src/main/resources/db/migration
flyway.baselineOnMigrate=true
flyway.validateOnMigrate=true
```

#### 执行迁移
```bash
# 1. 验证迁移脚本
mvn flyway:validate -Dflyway.configFiles=flyway-prod.conf

# 2. 查看待执行的迁移
mvn flyway:info -Dflyway.configFiles=flyway-prod.conf

# 3. 执行迁移
mvn flyway:migrate -Dflyway.configFiles=flyway-prod.conf

# 4. 验证结果
mvn flyway:info -Dflyway.configFiles=flyway-prod.conf
```

### 方式2: 手动执行SQL

```bash
# 1. 进入psql
psql -h localhost -U promanage -d promanage_prod

# 2. 开启事务
BEGIN;

# 3. 执行迁移脚本
\i /path/to/V1.1.0__organization_module_refactor.sql

# 4. 检查结果
SELECT COUNT(*) FROM organizations WHERE tenant_id IS NOT NULL;
SELECT COUNT(*) FROM organization_audit_logs;

# 5. 提交或回滚
COMMIT;  -- 或 ROLLBACK;
```

---

## ✅ 迁移验证

### 1. 结构验证
```sql
-- 检查新增字段
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'organizations'
  AND column_name IN ('tenant_id', 'version');

-- 检查索引
SELECT 
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'organizations'
  AND indexname LIKE 'idx_org_%';

-- 检查约束
SELECT 
    conname,
    contype,
    pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'organizations'::regclass;
```

### 2. 数据完整性验证
```sql
-- 验证tenant_id填充
SELECT 
    COUNT(*) as total,
    COUNT(tenant_id) as with_tenant_id,
    COUNT(*) - COUNT(tenant_id) as missing_tenant_id
FROM organizations
WHERE deleted_at IS NULL;
-- 预期: missing_tenant_id = 0

-- 验证settings转换
SELECT 
    id,
    name,
    jsonb_typeof(settings) as settings_type
FROM organizations
WHERE deleted_at IS NULL
  AND settings IS NOT NULL
LIMIT 10;
-- 预期: settings_type = 'object'

-- 验证version字段
SELECT 
    COUNT(*) as total,
    COUNT(version) as with_version,
    MIN(version) as min_version,
    MAX(version) as max_version
FROM organizations;
-- 预期: with_version = total, min_version = 0
```

### 3. 性能验证
```sql
-- 测试租户隔离查询
EXPLAIN ANALYZE
SELECT * FROM organizations
WHERE tenant_id = 1 AND deleted_at IS NULL
ORDER BY created_at DESC
LIMIT 20;
-- 预期: 使用 idx_org_tenant_active 索引

-- 测试slug查询
EXPLAIN ANALYZE
SELECT * FROM organizations
WHERE slug = 'test-org' AND deleted_at IS NULL;
-- 预期: 使用 idx_org_slug_unique_active 索引

-- 测试订阅查询
EXPLAIN ANALYZE
SELECT * FROM organizations
WHERE subscription_expires_at <= CURRENT_TIMESTAMP + INTERVAL '7 days'
  AND subscription_expires_at > CURRENT_TIMESTAMP
  AND deleted_at IS NULL;
-- 预期: 使用 idx_org_subscription_expires 索引
```

### 4. 审计日志验证
```sql
-- 检查审计表结构
\d organization_audit_logs

-- 插入测试记录
INSERT INTO organization_audit_logs 
(organization_id, tenant_id, action, actor_id, actor_name, changes)
VALUES 
(1, 1, 'TEST', 1, 'Test User', '{"test": true}'::jsonb);

-- 验证查询
SELECT * FROM organization_audit_logs WHERE action = 'TEST';

-- 清理测试数据
DELETE FROM organization_audit_logs WHERE action = 'TEST';
```

---

## 📊 性能对比测试

### 测试脚本
```sql
-- 创建测试函数
CREATE OR REPLACE FUNCTION test_organization_queries()
RETURNS TABLE(
    query_name TEXT,
    execution_time_ms NUMERIC
) AS $$
DECLARE
    start_time TIMESTAMP;
    end_time TIMESTAMP;
BEGIN
    -- 测试1: 租户隔离查询
    start_time := clock_timestamp();
    PERFORM * FROM organizations 
    WHERE tenant_id = 1 AND deleted_at IS NULL 
    LIMIT 100;
    end_time := clock_timestamp();
    query_name := 'tenant_isolation_query';
    execution_time_ms := EXTRACT(MILLISECONDS FROM (end_time - start_time));
    RETURN NEXT;
    
    -- 测试2: slug查询
    start_time := clock_timestamp();
    PERFORM * FROM organizations 
    WHERE slug = 'demo-org' AND deleted_at IS NULL;
    end_time := clock_timestamp();
    query_name := 'slug_query';
    execution_time_ms := EXTRACT(MILLISECONDS FROM (end_time - start_time));
    RETURN NEXT;
    
    -- 测试3: 订阅过期查询
    start_time := clock_timestamp();
    PERFORM * FROM organizations 
    WHERE subscription_expires_at <= CURRENT_TIMESTAMP + INTERVAL '7 days'
      AND deleted_at IS NULL;
    end_time := clock_timestamp();
    query_name := 'subscription_expiry_query';
    execution_time_ms := EXTRACT(MILLISECONDS FROM (end_time - start_time));
    RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

-- 执行测试
SELECT * FROM test_organization_queries();
```

### 基准对比
| 查询类型 | 迁移前(ms) | 迁移后(ms) | 改善 |
|---------|-----------|-----------|------|
| 租户隔离查询 | ___ | ___ | ___% |
| slug查询 | ___ | ___ | ___% |
| 订阅过期查询 | ___ | ___ | ___% |

---

## 🔄 回滚流程

### 场景1: 迁移失败(事务中)
```sql
-- 如果在事务中执行,直接回滚
ROLLBACK;
```

### 场景2: 迁移完成但发现问题
```bash
# 1. 停止应用
kubectl scale deployment/promanage-api --replicas=0

# 2. 执行回滚脚本
psql -h localhost -U promanage -d promanage_prod \
  -f db/migration/V1.1.0__organization_module_refactor_rollback.sql

# 3. 验证回滚
psql -h localhost -U promanage -d promanage_prod -c "
  SELECT column_name 
  FROM information_schema.columns 
  WHERE table_name = 'organizations' 
    AND column_name IN ('tenant_id', 'version');
"
# 预期: 根据回滚策略,可能保留或删除这些字段

# 4. 恢复备份(如需要)
pg_restore -h localhost -U promanage -d promanage_prod \
  -c -v /backup/promanage/YYYYMMDD/promanage_full_HHMMSS.backup
```

### 场景3: 数据损坏
```bash
# 1. 创建新数据库
createdb -h localhost -U promanage promanage_prod_new

# 2. 恢复备份到新库
pg_restore -h localhost -U promanage -d promanage_prod_new \
  -v /backup/promanage/YYYYMMDD/promanage_full_HHMMSS.backup

# 3. 切换数据库连接
# 修改应用配置指向 promanage_prod_new

# 4. 验证后删除旧库
dropdb -h localhost -U promanage promanage_prod
```

---

## 📝 迁移记录模板

```markdown
## 迁移执行记录

**执行日期**: YYYY-MM-DD HH:MM:SS  
**执行人**: ___________  
**环境**: Production  

### 迁移前状态
- 数据库版本: ___________
- organizations表行数: ___________
- 表大小: ___________
- 备份文件: ___________

### 迁移执行
- 开始时间: ___________
- 结束时间: ___________
- 执行耗时: ___________
- 是否成功: [ ] 是 [ ] 否

### 验证结果
- [ ] 结构验证通过
- [ ] 数据完整性验证通过
- [ ] 性能验证通过
- [ ] 审计日志验证通过

### 问题记录
___________

### 签名
执行人: ___________  
审核人: ___________
```

---

## 🛠️ 故障排查

### 问题1: 索引创建超时
```sql
-- 检查索引创建进度
SELECT 
    pid,
    now() - pg_stat_activity.query_start AS duration,
    query
FROM pg_stat_activity
WHERE query LIKE '%CREATE INDEX%';

-- 如果超时,取消并使用更小的maintenance_work_mem
SET maintenance_work_mem = '256MB';
CREATE INDEX CONCURRENTLY idx_org_tenant_active 
ON organizations(tenant_id, deleted_at) 
WHERE deleted_at IS NULL;
```

### 问题2: settings转换失败
```sql
-- 查找无效JSON
SELECT id, name, settings
FROM organizations
WHERE settings IS NOT NULL
  AND settings !~ '^\s*\{';

-- 手动修复
UPDATE organizations
SET settings = NULL
WHERE id IN (/* 无效记录的ID */);
```

### 问题3: 锁等待
```sql
-- 查看锁等待
SELECT 
    blocked_locks.pid AS blocked_pid,
    blocked_activity.usename AS blocked_user,
    blocking_locks.pid AS blocking_pid,
    blocking_activity.usename AS blocking_user,
    blocked_activity.query AS blocked_statement,
    blocking_activity.query AS blocking_statement
FROM pg_catalog.pg_locks blocked_locks
JOIN pg_catalog.pg_stat_activity blocked_activity ON blocked_activity.pid = blocked_locks.pid
JOIN pg_catalog.pg_locks blocking_locks 
    ON blocking_locks.locktype = blocked_locks.locktype
    AND blocking_locks.database IS NOT DISTINCT FROM blocked_locks.database
    AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation
    AND blocking_locks.pid != blocked_locks.pid
JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid
WHERE NOT blocked_locks.granted;

-- 终止阻塞进程(谨慎使用)
SELECT pg_terminate_backend(blocking_pid);
```

---

## 📞 应急联系

- **DBA**: ___________
- **技术负责人**: ___________
- **备用DBA**: ___________
