# 后端安全与架构修复方案

**文档目的**: 本文档旨在为 `BACKEND_AUDIT_REPORT_20251020.md` 中发现的核心安全问题和架构不一致性，提供一个清晰、可执行的修复计划。

**核心目标**: 
1.  统一应用的授权机制，消除混乱和不可靠的自定义实现。
2.  重构权限模块，使其完全支持多租户，从根本上修复数据隔离缺陷。

---

## 修复原则

1.  **单一职责**: 严格遵守分层架构，控制器只负责请求路由和基础校验，所有业务和授权逻辑都应在服务层实现。
2.  **标准优先**: 优先使用Spring Security等行业标准框架提供的成熟功能，而不是“造轮子”。
3.  **租户隔离**: 所有数据操作都必须以租户（`organizationId`）为第一隔离单位。
4.  **显式授权**: 必须在每个端点上明确声明其所需的权限。

---

## 阶段一：统一授权机制

**目标**: 废弃自定义的 `@RequirePermission` 注解和 `PermissionAspect`，全面迁移到Spring Security的 `@PreAuthorize` 注解。

### 步骤 1.1: 创建一个集中的权限检查服务

创建一个新的服务 `PermissionCheckService`，它将作为Spring Security表达式语言（SpEL）的扩展点。

```java
// File: backend/promanage-service/src/main/java/com/promanage/service/security/PermissionCheckService.java

@Service("permissionCheck") // "permissionCheck" 是在SpEL中调用的bean名称
@RequiredArgsConstructor
public class PermissionCheckService {

    private final IUserService userService;

    /**
     * 检查当前登录用户是否拥有指定权限。
     * @param permissionCode 权限编码
     * @return boolean
     */
    public boolean hasPermission(String permissionCode) {
        Long userId = SecurityUtils.getCurrentUserId().orElse(null);
        if (userId == null) {
            return false;
        }
        return userService.hasPermission(userId, permissionCode);
    }

    /**
     * 检查当前用户是否是指定项目的成员。
     * (示例：未来可以扩展更多针对具体资源的安全检查)
     * @param projectId 项目ID
     * @return boolean
     */
    public boolean isProjectMember(Long projectId) {
        Long userId = SecurityUtils.getCurrentUserId().orElse(null);
        if (userId == null || projectId == null) {
            return false;
        }
        // 假设 IProjectService 提供了检查方法
        // return projectService.isMember(projectId, userId);
        return true; // 占位符
    }
}
```

### 步骤 1.2: 迁移 `@RequirePermission` 到 `@PreAuthorize`

在整个项目中，特别是 `DocumentController`，将所有 `@RequirePermission` 注解替换为等效的 `@PreAuthorize` 注解。

**示例 (修改 `DocumentController.java`)**:

**修改前:**
```java
@GetMapping("/documents/{documentId}")
@Operation(summary = "获取文档详情")
@RequirePermission("document:view")
public Result<DocumentDetailResponse> getDocument(@PathVariable Long documentId) { ... }
```

**修改后:**
```java
@GetMapping("/documents/{documentId}")
@Operation(summary = "获取文档详情")
@PreAuthorize("@permissionCheck.hasPermission('document:view')")
public Result<DocumentDetailResponse> getDocument(@PathVariable Long documentId) { ... }
```

### 步骤 1.3: 删除自定义授权实现

在完成所有注解的迁移后，安全地删除以下文件：
- `backend/promanage-infrastructure/src/main/java/com/promanage/infrastructure/security/RequirePermission.java`
- `backend/promanage-service/src/main/java/com/promanage/service/aspect/PermissionAspect.java`

---

## 阶段二：重构权限模块以支持多租户

**目标**: 彻底改造 `Permission` 模块，使其与 `Organization` 模块的设计保持一致，实现租户级的数据隔离。

### 步骤 2.1: 数据库变更

使用Flyway或手动执行SQL，为 `t_permission` 表（假设表名）添加 `organization_id` 字段。

```sql
ALTER TABLE t_permission ADD COLUMN organization_id BIGINT;

-- 为现有权限设置一个默认的组织ID，或将其设置为NULL表示系统级权限
-- UPDATE t_permission SET organization_id = 1 WHERE organization_id IS NULL;

CREATE INDEX idx_permission_org ON t_permission (organization_id);
```

### 步骤 2.2: 实体类与DTO修改

- 为 `Permission` 实体类添加 `organizationId` 字段。
- 为 `PermissionResponse`, `CreatePermissionRequest`, `UpdatePermissionRequest` 等DTO添加 `organizationId` 字段。

### 步骤 2.3: 重构 `PermissionManagementServiceImpl`

修改服务层，使其所有操作都与 `organizationId` 绑定。

**示例 (修改 `IPermissionManagementService` 和 `PermissionManagementServiceImpl`)**:

```java
// 接口修改
List<PermissionResponse> listPermissions(Long organizationId);
PermissionResponse getPermission(Long organizationId, Long permissionId);
Long createPermission(Long organizationId, CreatePermissionRequest request);
// ... 其他方法同样需要修改

// 实现类修改
@Override
public List<PermissionResponse> listPermissions(Long organizationId) {
    // 授权检查：确认当前用户是该组织的成员/管理员
    assertOrganizationAdmin(SecurityUtils.getCurrentUserId().get(), organizationId);

    LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Permission::getOrganizationId, organizationId) // **核心过滤条件**
                  .eq(Permission::getDeleted, false)
                  .orderByAsc(Permission::getSort);
    // ...
}
```

### 步骤 2.4: 重写 `PermissionController`

根据重构后的服务层，彻底重写 `PermissionController`。

- **移除所有业务逻辑**: 控制器中不应再有创建或修改实体的逻辑。
- **传递租户上下文**: 从路径变量或当前用户的上下文中获取 `organizationId`，并将其传递给服务层。
- **修复损坏的逻辑**: 重新实现 `updatePermission` 和 `getRolePermissions` 等方法的正确逻辑。

**示例 (重写 `PermissionController.java` 中的一个方法)**:

```java
@GetMapping("/organizations/{organizationId}/permissions/tree")
@Operation(summary = "获取指定组织的权限树")
@PreAuthorize("@permissionCheck.hasPermission('permission:view')")
public Result<List<PermissionTreeResponse>> getPermissionTree(@PathVariable Long organizationId) {
    // 授权检查将由服务层处理，确保当前用户可以访问该 organizationId
    List<PermissionTreeResponse> permissionTree = permissionManagementService.getPermissionTree(organizationId);
    return Result.success(permissionTree);
}
```

---

## 阶段三：验证与巩固

1.  **代码审查**: 对所有修改过的代码进行交叉审查，确保修复方案被正确实施。
2.  **集成测试**: 编写新的集成测试，专门用于验证多租户隔离是否生效。例如：
    - 尝试让A组织的用户获取B组织的权限列表，预期失败。
    - 尝试让A组织的用户为B组织的角色分配权限，预期失败。
3.  **回归测试**: 执行完整的回归测试，确保修复没有破坏现有功能。

---

**修复建议的优先级**: **最高**。建议在进行任何新的功能开发之前，优先完成此修复方案。
