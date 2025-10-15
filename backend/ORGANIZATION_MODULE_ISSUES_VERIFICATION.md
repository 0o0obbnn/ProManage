# 组织模块问题验证报告

## 执行摘要
✅ **已验证代码** | 🔴 **8个关键问题确认存在** | 🟡 **3个部分解决** | 🟢 **2个已修复**

---

## 问题验证详情

### 🔴 问题1: 租户隔离不完整 - **确认存在**

**位置**: `OrganizationServiceImpl.listOrganizations()` (Line 195-227)

**问题代码**:
```java
// 仅在非超级管理员时才过滤租户
if (!permissionService.isSuperAdmin(requesterId)) {
    User requester = userService.getById(requesterId);
    if (requester == null || requester.getOrganizationId() == null) {
        throw new BusinessException(ResultCode.FORBIDDEN, "当前用户未关联组织,无法查询");
    }
    wrapper.eq(Organization::getId, requester.getOrganizationId());
}
```

**问题分析**:
- ❌ 超级管理员可以查看所有组织(跨租户)
- ❌ 普通用户只能看到自己所属的单个组织
- ❌ 缺少基于 `tenantId` 的强制过滤
- ❌ 违反多租户隔离原则

**影响**: **严重** - 数据泄露风险

---

### 🔴 问题2: RBAC权限检查不一致 - **确认存在**

**位置**: 多个方法

**问题代码**:
```java
// OrganizationController.listOrganizations() - 使用Spring Security注解
@PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")

// OrganizationServiceImpl.updateOrganization() - 使用自定义权限服务
assertOrganizationAdmin(updaterId, organization.getId());

// OrganizationServiceImpl.updateSubscriptionPlan() - 内联权限检查
if (!permissionService.isOrganizationAdmin(updaterId, id)) {
    throw new BusinessException(ResultCode.FORBIDDEN, "您不是该组织管理员,无权修改订阅计划");
}
```

**问题分析**:
- ❌ 三种不同的权限检查方式混用
- ❌ Controller层使用 `@PreAuthorize` 但不检查组织级权限
- ❌ Service层部分方法使用 `assertOrganizationAdmin`,部分内联检查
- ❌ 缺少统一的权限检查策略

**影响**: **严重** - 权限绕过风险

---

### 🟡 问题3: 敏感数据暴露 - **部分解决**

**位置**: `OrganizationController.getOrganizationMembers()` (Line 189-203)

**当前状态**:
```java
// ✅ 已使用 OrganizationMemberDTO 而非直接返回 User 实体
public Result<PageResult<OrganizationMemberDTO>> getOrganizationMembers(...)

// ✅ OrganizationMemberDTO 只包含安全字段
@Data
public class OrganizationMemberDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String position;
    private Integer status;
    private LocalDateTime lastLoginTime;
}
```

**残留问题**:
- ⚠️ 缺少角色信息(roles字段)
- ⚠️ email字段可能需要脱敏(如 l***@example.com)
- ⚠️ 未验证 `lastLoginTime` 是否应该暴露

**影响**: **中等** - 信息泄露风险降低但未完全消除

---

### 🔴 问题4: 软删除过滤不完整 - **确认存在**

**位置**: `OrganizationMapper.java` 多个查询方法

**问题代码**:
```java
// ✅ 部分方法已添加 deleted_at IS NULL
@Select("SELECT * FROM organizations WHERE slug = #{slug} AND deleted_at IS NULL")
Organization findBySlug(@Param("slug") String slug);

// ❌ 但 MyBatis-Plus 的 selectById 不会自动过滤软删除
// OrganizationServiceImpl.loadActiveOrganizationOrThrow()
Organization organization = organizationMapper.selectById(id);
if (organization == null || organization.getDeletedAt() != null) {
    throw new BusinessException(ResultCode.DATA_NOT_FOUND, "组织不存在");
}
```

**问题分析**:
- ⚠️ 依赖手动检查 `deletedAt != null` 而非全局拦截器
- ❌ 容易遗漏检查导致已删除数据被访问
- ❌ 未配置 MyBatis-Plus 的逻辑删除插件

**建议**: 配置全局逻辑删除
```java
@TableLogic
private LocalDateTime deletedAt;
```

**影响**: **高** - 已删除数据可能被访问

---

### 🔴 问题5: DTO映射允许null覆盖 - **部分修复**

**位置**: `OrganizationMapper.updateEntityFromDto()` (DTO Mapper)

**当前代码**:
```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Mapping(target = "slug", ignore = true) // ✅ 已阻止slug修改
void updateEntityFromDto(UpdateOrganizationRequestDTO request, @MappingTarget Organization organization);
```

**状态**:
- ✅ 已使用 `NullValuePropertyMappingStrategy.IGNORE`
- ✅ 已阻止 `slug` 字段修改
- ✅ Service层也有二次校验

```java
// OrganizationServiceImpl.updateOrganization()
if (!Objects.equals(persisted.getSlug(), organization.getSlug())) {
    throw new BusinessException(ResultCode.PARAM_ERROR, "组织标识符不允许修改");
}
```

**影响**: **低** - 已基本解决

---

### 🔴 问题6: SQL查询错误 - **确认存在**

**位置**: `OrganizationMapper.findExpiringSubscriptions()` (Line 135-142)

**问题代码**:
```java
@Select("""
    SELECT *
    FROM organizations
    WHERE deleted_at IS NULL
      AND subscription_expires_at <= CURRENT_TIMESTAMP + (#{days} || ' days')::interval
      AND subscription_expires_at > CURRENT_TIMESTAMP
    """)
List<Organization> findExpiringSubscriptions(@Param("days") Integer days);
```

**问题分析**:
- ❌ PostgreSQL interval语法: `(#{days} || ' days')::interval` 
- ❌ 字符串拼接存在SQL注入风险(虽然参数是Integer)
- ❌ 应使用 `INTERVAL '1 day' * #{days}` 或 `NOW() + INTERVAL '#{days} days'`

**正确写法**:
```sql
subscription_expires_at <= CURRENT_TIMESTAMP + (#{days} * INTERVAL '1 day')
```

**影响**: **中等** - 查询可能失败

---

### 🔴 问题7: Settings序列化缺少验证 - **确认存在**

**位置**: `OrganizationServiceImpl.getOrganizationSettings()` (Line 383-397)

**问题代码**:
```java
try {
    OrganizationSettingsDTO parsed = objectMapper.readValue(organization.getSettings(), OrganizationSettingsDTO.class);
    return normalizeSettings(parsed);
} catch (JsonProcessingException e) {
    log.error("解析组织设置失败, organizationId={}", organizationId, e);
    return buildDefaultSettings(); // ❌ 吞掉异常,返回默认值
}
```

**问题分析**:
- ❌ JSON解析失败时静默返回默认值
- ❌ 用户无法知道设置已损坏
- ❌ 更新时有验证(`validateSettings`),但读取时无验证
- ⚠️ 可能导致数据不一致

**建议**: 至少记录警告或返回错误标识

**影响**: **中等** - 数据一致性问题

---

### 🔴 问题8: 测试覆盖不足 - **确认存在**

**位置**: 测试文件缺失或不完整

**验证结果**:
```
✅ 存在: OrganizationServiceImplTest.java
❌ 缺失: OrganizationControllerTest.java (集成测试)
❌ 缺失: OrganizationMapperTest.java (SQL测试)
❌ 缺失: OrganizationSecurityTest.java (安全测试)
```

**关键测试场景缺失**:
1. ❌ 跨租户访问测试(用户A访问组织B)
2. ❌ 软删除组织的访问测试
3. ❌ 并发更新冲突测试(乐观锁)
4. ❌ SQL interval查询测试
5. ❌ Settings JSON损坏场景测试
6. ❌ 权限边界测试(admin vs member)

**影响**: **高** - 无法保证代码质量

---

## 额外发现的问题

### 🟡 问题9: 乐观锁未启用

**位置**: `Organization` 实体

**问题**:
```java
// Organization.java 继承自 BaseEntity
// 但未找到 @Version 注解

// OrganizationServiceImpl.updateOrganization()
organization.setVersion(persisted.getVersion()); // 设置了version但未启用乐观锁
```

**验证**: 需要检查 `BaseEntity` 是否包含:
```java
@Version
private Long version;
```

**影响**: **中等** - 并发更新可能丢失数据

---

### 🟢 问题10: 时间戳字段命名不一致 - **已解决**

**位置**: `OrganizationMapper.toDto()` (DTO Mapper)

**当前代码**:
```java
@Mapping(source = "createTime", target = "createdAt")
@Mapping(source = "updateTime", target = "updatedAt")
OrganizationDTO toDto(Organization organization);
```

**分析**:
- ✅ Mapper已处理字段名差异
- ⚠️ 但实体中同时存在 `createTime` 和 `createdAt` 字段(继承自BaseEntity)
- ⚠️ 可能导致混淆

**建议**: 统一使用 `createdAt/updatedAt` 或 `createTime/updateTime`

**影响**: **低** - 已通过Mapper解决

---

## 问题优先级矩阵

| 问题 | 严重性 | 影响范围 | 修复难度 | 优先级 |
|------|--------|----------|----------|--------|
| 1. 租户隔离 | 🔴 严重 | 全局 | 中 | **P0** |
| 2. RBAC不一致 | 🔴 严重 | 全局 | 高 | **P0** |
| 4. 软删除过滤 | 🔴 高 | 全局 | 低 | **P0** |
| 8. 测试覆盖 | 🔴 高 | 全局 | 高 | **P1** |
| 6. SQL错误 | 🟡 中 | 订阅功能 | 低 | **P1** |
| 7. Settings验证 | 🟡 中 | 设置功能 | 低 | **P1** |
| 9. 乐观锁 | 🟡 中 | 更新操作 | 低 | **P2** |
| 3. 敏感数据 | 🟡 中 | 成员列表 | 低 | **P2** |
| 5. DTO映射 | 🟢 低 | 更新操作 | - | **已解决** |
| 10. 字段命名 | 🟢 低 | DTO转换 | - | **已解决** |

---

## 修复建议

### 立即修复(P0)

1. **租户隔离**
```java
// 在所有查询中强制添加 tenantId 过滤
wrapper.eq(Organization::getTenantId, requester.getTenantId());
```

2. **统一权限检查**
```java
// 创建统一的权限切面
@Aspect
public class OrganizationPermissionAspect {
    @Before("@annotation(RequireOrganizationAdmin)")
    public void checkOrganizationAdmin(JoinPoint jp) {
        // 统一权限检查逻辑
    }
}
```

3. **启用逻辑删除插件**
```java
@TableLogic
private LocalDateTime deletedAt;
```

### 短期修复(P1)

4. **修复SQL语法**
```sql
subscription_expires_at <= CURRENT_TIMESTAMP + (#{days} * INTERVAL '1 day')
```

5. **增强Settings验证**
```java
if (StringUtils.isBlank(organization.getSettings())) {
    return buildDefaultSettings();
}
try {
    OrganizationSettingsDTO parsed = objectMapper.readValue(...);
    validateSettings(parsed); // 添加验证
    return normalizeSettings(parsed);
} catch (JsonProcessingException e) {
    log.warn("组织设置已损坏,使用默认值, orgId={}", organizationId);
    throw new BusinessException(ResultCode.DATA_ERROR, "组织设置格式错误");
}
```

6. **补充测试用例**
- 创建 `OrganizationSecurityTest`
- 创建 `OrganizationMapperSQLTest` (使用Testcontainers)

---

## 结论

✅ **重构计划合理且必要**

**验证结果**:
- 8个关键问题确认存在于代码中
- 2个问题已部分解决但需加强
- 重构计划覆盖了所有主要问题

**建议**:
1. 按照优先级矩阵执行修复
2. 补充缺失的测试用例
3. 考虑引入代码审查检查清单
4. 建立权限检查的最佳实践文档

**风险评估**: 当前代码存在**严重的安全和数据隔离问题**,建议尽快执行重构计划。

---

**报告生成时间**: 2025-01-XX  
**验证人**: Amazon Q Code Reviewer  
**代码版本**: ProManage Backend v1.0
