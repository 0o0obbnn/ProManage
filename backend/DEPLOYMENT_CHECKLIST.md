# 组织模块重构部署检查清单

## 📋 部署前检查 (Pre-Deployment)

### 1. 代码审查 ✅
- [ ] 所有代码已通过Code Review
- [ ] 单元测试覆盖率 ≥ 80%
- [ ] 集成测试全部通过
- [ ] 安全测试通过(租户隔离、权限检查)
- [ ] 性能测试基准达标

### 2. 数据库准备 ✅
- [ ] 备份生产数据库
  ```bash
  pg_dump -h localhost -U promanage -d promanage_prod > backup_$(date +%Y%m%d_%H%M%S).sql
  ```
- [ ] 在测试环境验证迁移脚本
- [ ] 检查磁盘空间(至少预留20%空闲)
- [ ] 确认数据库连接池配置
- [ ] 验证索引创建时间(CONCURRENTLY模式)

### 3. 配置变更 ✅

#### application.yml 新增配置
```yaml
# 多租户配置
promanage:
  tenant:
    isolation-enabled: true
    default-tenant-id: 1
  
  # 组织设置默认值
  organization:
    default-subscription-plan: FREE
    max-members-per-org: 50
    storage-limit-mb: 10240
  
  # 审计日志
  audit:
    enabled: true
    retention-days: 90
```

#### MyBatis-Plus 逻辑删除配置
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deletedAt
      logic-delete-value: now()
      logic-not-delete-value: null
```

### 4. 依赖检查 ✅
- [ ] Flyway版本: ≥ 9.0.0
- [ ] MyBatis-Plus版本: ≥ 3.5.0
- [ ] Jackson版本: ≥ 2.15.0
- [ ] Spring Boot版本: ≥ 3.0.0
- [ ] Docker版本: ≥ 20.10.0
- [ ] Docker Compose版本: ≥ 1.29.0

### 5. 监控指标配置 ✅

#### Prometheus指标
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: promanage
      module: organization
    export:
      prometheus:
        enabled: true
```

#### 自定义指标
- `organization_list_query_duration_seconds` - 组织列表查询耗时
- `organization_member_count` - 组织成员数量
- `organization_settings_update_total` - 设置更新次数
- `organization_audit_log_total` - 审计日志记录数

### 6. 告警规则配置 ✅

```yaml
# Prometheus Alert Rules
groups:
  - name: organization_module
    rules:
      - alert: OrganizationQuerySlow
        expr: histogram_quantile(0.95, organization_list_query_duration_seconds) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "组织查询响应慢"
          
      - alert: TenantIsolationViolation
        expr: rate(organization_cross_tenant_access_total[5m]) > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "检测到跨租户访问"
```

---

## 🚀 部署执行 (Deployment)

### 阶段1: 数据库迁移 (停机窗口)

**预计时间**: 10-15分钟

```bash
# 1. 进入维护模式
curl -X POST http://api.promanage.com/admin/maintenance/enable

# 2. 执行迁移
cd backend/promanage-api
mvn flyway:migrate -Dflyway.configFiles=flyway-prod.conf

# 3. 验证迁移
psql -h localhost -U promanage -d promanage_prod -c "
  SELECT version, description, installed_on, success 
  FROM flyway_schema_history 
  ORDER BY installed_rank DESC LIMIT 5;
"

# 4. 检查索引创建状态
psql -h localhost -U promanage -d promanage_prod -c "
  SELECT schemaname, tablename, indexname, indexdef 
  FROM pg_indexes 
  WHERE tablename = 'organizations' 
  AND indexname LIKE 'idx_org_%';
"
```

### 阶段2: 应用部署 (灰度发布)

#### 方式1: 使用自动化部署脚本 (推荐)
```bash
# 构建并部署到开发环境
cd backend/promanage-api
./deploy.sh -e dev -b -d

# 或使用PowerShell版本 (Windows)
powershell -File deploy.ps1 -Environment dev -Build -Deploy

# 部署到生产环境
./deploy.sh -e prod -d

# 执行健康检查
./deploy.sh -e prod -c

# 执行回滚
./deploy.sh -e prod -R
```

#### 方式2: 手动部署
#### Step 1: 部署到Canary环境 (10%流量)
```bash
# 构建镜像
mvn clean package -DskipTests
docker build -t promanage-api:v1.1.0 .

# 部署Canary
kubectl set image deployment/promanage-api-canary \
  promanage-api=promanage-api:v1.1.0

# 等待Pod就绪
kubectl rollout status deployment/promanage-api-canary
```

**验证指标** (观察30分钟):
- [ ] 错误率 < 0.1%
- [ ] P95延迟 < 500ms
- [ ] CPU使用率 < 70%
- [ ] 内存使用率 < 80%

#### Step 2: 扩大到50%流量
```bash
kubectl patch service promanage-api -p '
{
  "spec": {
    "selector": {
      "version": "v1.1.0"
    }
  }
}'
```

**验证指标** (观察1小时):
- [ ] 无跨租户访问告警
- [ ] 权限检查通过率 100%
- [ ] 数据库连接池健康

#### Step 3: 全量发布
```bash
kubectl set image deployment/promanage-api \
  promanage-api=promanage-api:v1.1.0

kubectl rollout status deployment/promanage-api
```

### 阶段3: 功能验证

#### 自动化测试
```bash
# 运行冒烟测试
cd backend/promanage-service
mvn test -Dtest=OrganizationSmokeTest

# 运行安全测试
mvn test -Dtest=OrganizationSecurityTest
```

#### 手动验证清单
- [ ] 创建组织(验证tenant_id自动填充)
- [ ] 更新组织(验证乐观锁)
- [ ] 删除组织(验证软删除)
- [ ] 查询组织列表(验证租户隔离)
- [ ] 获取组织成员(验证权限检查)
- [ ] 更新组织设置(验证JSONB存储)
- [ ] 跨租户访问测试(应返回403)

---

## 📊 部署后监控 (Post-Deployment)

### 1. 实时监控 (前24小时)

#### 关键指标
```sql
-- 查询性能监控
SELECT 
    query,
    calls,
    mean_exec_time,
    max_exec_time
FROM pg_stat_statements
WHERE query LIKE '%organizations%'
ORDER BY mean_exec_time DESC
LIMIT 10;

-- 索引使用情况
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE tablename = 'organizations';

-- 审计日志统计
SELECT 
    action,
    COUNT(*) as count,
    DATE_TRUNC('hour', created_at) as hour
FROM organization_audit_logs
WHERE created_at > NOW() - INTERVAL '24 hours'
GROUP BY action, hour
ORDER BY hour DESC;
```

#### 应用日志监控
```bash
# 检查错误日志
kubectl logs -l app=promanage-api --tail=100 | grep -i "error\|exception"

# 检查租户隔离日志
kubectl logs -l app=promanage-api --tail=1000 | grep "tenant"

# 检查权限检查日志
kubectl logs -l app=promanage-api --tail=1000 | grep "permission\|forbidden"
```

### 2. 性能基准对比

| 指标 | 重构前 | 重构后 | 目标 | 状态 |
|------|--------|--------|------|------|
| 组织列表查询(P95) | 350ms | ___ ms | <200ms | ⏳ |
| 成员列表查询(P95) | 280ms | ___ ms | <300ms | ⏳ |
| 设置更新(P95) | 120ms | ___ ms | <150ms | ⏳ |
| 数据库连接数 | 45 | ___ | <50 | ⏳ |
| 内存使用 | 1.2GB | ___ GB | <1.5GB | ⏳ |

### 3. 数据完整性检查

```sql
-- 检查tenant_id完整性
SELECT COUNT(*) as missing_tenant_id
FROM organizations
WHERE tenant_id IS NULL AND deleted_at IS NULL;
-- 预期: 0

-- 检查软删除一致性
SELECT COUNT(*) as inconsistent_deletes
FROM organizations
WHERE deleted_at IS NOT NULL AND deleted_by IS NULL;
-- 预期: 0

-- 检查settings格式
SELECT id, name
FROM organizations
WHERE deleted_at IS NULL 
  AND (settings IS NULL OR NOT jsonb_typeof(settings) = 'object');
-- 预期: 0行

-- 检查版本号
SELECT COUNT(*) as missing_version
FROM organizations
WHERE version IS NULL;
-- 预期: 0
```

---

## 🔄 回滚计划 (Rollback Plan)

### 触发条件
- 错误率 > 1%
- P95延迟 > 1000ms
- 发现数据泄露或跨租户访问
- 关键功能不可用

### 回滚步骤

#### 快速回滚(应用层)
```bash
# 1. 回滚到上一版本
kubectl rollout undo deployment/promanage-api

# 2. 验证回滚
kubectl rollout status deployment/promanage-api

# 3. 检查服务健康
curl http://api.promanage.com/actuator/health
```

#### 完整回滚(含数据库)
```bash
# 1. 停止应用
kubectl scale deployment/promanage-api --replicas=0

# 2. 执行回滚脚本
psql -h localhost -U promanage -d promanage_prod \
  -f db/migration/V1.1.0__organization_module_refactor_rollback.sql

# 3. 恢复数据库备份(如需要)
psql -h localhost -U promanage -d promanage_prod < backup_YYYYMMDD_HHMMSS.sql

# 4. 重启旧版本应用
kubectl set image deployment/promanage-api promanage-api=promanage-api:v1.0.0
kubectl scale deployment/promanage-api --replicas=3
```

---

## 📝 部署记录

### 部署信息
- **部署日期**: ___________
- **部署人员**: ___________
- **版本号**: v1.1.0
- **环境**: Production

### 检查点签名

| 阶段 | 负责人 | 完成时间 | 签名 | 备注 |
|------|--------|----------|------|------|
| 数据库备份 | | | | |
| 迁移脚本执行 | | | | |
| Canary部署 | | | | |
| 50%流量验证 | | | | |
| 全量发布 | | | | |
| 功能验证 | | | | |
| 监控配置 | | | | |

### 问题记录

| 时间 | 问题描述 | 影响范围 | 解决方案 | 状态 |
|------|----------|----------|----------|------|
| | | | | |

---

## 📞 应急联系人

- **技术负责人**: ___________
- **DBA**: ___________
- **运维负责人**: ___________
- **产品负责人**: ___________

## 📚 相关文档

- [组织模块重构计划](./ORGANIZATION_MODULE_REFACTOR_PLAN.md)
- [问题验证报告](./ORGANIZATION_MODULE_ISSUES_VERIFICATION.md)
- [API变更文档](./API_CHANGES_v1.1.0.md)
- [数据库迁移指南](./DATABASE_MIGRATION_GUIDE.md)
