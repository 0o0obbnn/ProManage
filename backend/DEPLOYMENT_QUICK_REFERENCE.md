# 组织模块重构部署快速参考

## ⚡ 快速命令

### 部署前
```bash
# 1. 备份数据库
pg_dump -h localhost -U promanage -d promanage_prod > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. 验证迁移脚本
mvn flyway:validate -Dflyway.configFiles=flyway-prod.conf

# 3. 检查磁盘空间
df -h
```

### 部署执行
```bash
# 1. 进入维护模式
curl -X POST http://api.promanage.com/admin/maintenance/enable

# 2. 执行迁移
mvn flyway:migrate -Dflyway.configFiles=flyway-prod.conf

# 3. 部署应用
kubectl set image deployment/promanage-api promanage-api=promanage-api:v1.1.0
kubectl rollout status deployment/promanage-api

# 4. 退出维护模式
curl -X POST http://api.promanage.com/admin/maintenance/disable
```

### 验证
```bash
# 健康检查
curl http://api.promanage.com/actuator/health

# 功能测试
curl -H "Authorization: Bearer $TOKEN" http://api.promanage.com/api/v1/organizations
```

### 回滚
```bash
# 应用回滚
kubectl rollout undo deployment/promanage-api

# 数据库回滚
psql -h localhost -U promanage -d promanage_prod \
  -f db/migration/V1.1.0__organization_module_refactor_rollback.sql
```

---

## 📊 关键指标

### 必须监控
- 错误率 < 0.1%
- P95延迟 < 500ms
- 跨租户访问 = 0
- 数据库连接 < 50

### SQL验证
```sql
-- 检查tenant_id
SELECT COUNT(*) FROM organizations WHERE tenant_id IS NULL AND deleted_at IS NULL;
-- 预期: 0

-- 检查索引
SELECT indexname FROM pg_indexes WHERE tablename = 'organizations' AND indexname LIKE 'idx_org_%';
-- 预期: 5个索引

-- 检查审计表
SELECT COUNT(*) FROM organization_audit_logs;
-- 预期: ≥ 0
```

---

## 🚨 故障处理

### 迁移失败
```sql
ROLLBACK;  -- 如果在事务中
```

### 应用启动失败
```bash
# 查看日志
kubectl logs -l app=promanage-api --tail=100

# 回滚版本
kubectl rollout undo deployment/promanage-api
```

### 性能问题
```sql
-- 检查慢查询
SELECT query, mean_exec_time 
FROM pg_stat_statements 
WHERE query LIKE '%organizations%' 
ORDER BY mean_exec_time DESC LIMIT 5;
```

---

## 📞 应急联系
- 技术负责人: ___________
- DBA: ___________
- 运维: ___________

---

## 📝 检查清单

### 部署前 ✅
- [ ] 数据库已备份
- [ ] 迁移脚本已验证
- [ ] 磁盘空间充足(>30%空闲)
- [ ] 团队已通知

### 部署中 ✅
- [ ] 维护模式已启用
- [ ] 迁移执行成功
- [ ] 应用部署成功
- [ ] 健康检查通过

### 部署后 ✅
- [ ] 功能验证通过
- [ ] 性能指标正常
- [ ] 无错误日志
- [ ] 监控告警正常

---

**版本**: v1.1.0  
**更新时间**: 2025-01-XX
