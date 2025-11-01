# ProManage 后端重构与修复计划

**核心原则**:
1.  **原子性**: 每个步骤都是一个最小的、可独立验证的改动。
2.  **安全性**: 优先修复安全漏洞。
3.  **测试驱动**: 每个修复步骤都必须有对应的测试（单元测试或集成测试）来验证。
4.  **回归验证**: 每个步骤完成后，都将运行完整的后端测试套件，确保没有破坏现有功能。

---

### **阶段零：P0 - 紧急安全修复**

此阶段的目标是立即消除已发现的高危安全漏洞。

#### **任务 1：修复不安全的 `isProjectMember` 存根方法 (中危)**
*   **问题**: `PermissionCheckService.isProjectMember` 方法是存根，总是返回 `true`，可导致权限绕过。
*   **目标**: 实现该方法的正确逻辑。
*   **原子化步骤**:
    1.  在 `IProjectService` 接口中定义 `isMember(Long projectId, Long userId)` 方法。
    2.  在 `ProjectServiceImpl` 中实现 `isMember` 方法，通过查询数据库来验证用户是否是项目成员。
    3.  在 `PermissionCheckService` 中注入 `IProjectService`。
    4.  修改 `PermissionCheckService.isProjectMember` 的实现，调用 `projectService.isMember()`。
*   **验证**:
    1.  为 `ProjectServiceImpl.isMember` 编写单元测试。
    2.  为 `PermissionCheckService.isProjectMember` 编写单元测试，Mock `IProjectService` 的行为。
    3.  运行 `mvn verify` 确保所有测试通过。

#### **任务 2：修复权限缓存刷新机制缺陷 (高危)**
*   **问题**: 修改角色权限后，用户的权限缓存不会刷新，导致权限变更无法实时生效。
*   **目标**: 确保角色权限变更后，相关用户的缓存立即失效。
*   **原子化步骤**:
    1.  创建一个 `RolePermissionsChangedEvent` 事件类，它将携带被修改的 `roleId`。
    2.  在管理角色-权限关系的服务方法中（例如 `RoleServiceImpl.updatePermissions`），当一个角色的权限被修改后，通过 `ApplicationEventPublisher` 发布 `RolePermissionsChangedEvent` 事件。
    3.  创建一个 `PermissionCacheEvictionListener` 类，监听 `RolePermissionsChangedEvent` 事件。
    4.  在该监听器中，根据 `roleId` 查询所有关联的 `userId`。
    5.  遍历这些 `userId`，并使用 `CacheManager` 手动清除每个用户的 `"userPermissions"` 缓存。
*   **验证**:
    1.  编写一个集成测试，模拟以下场景：
        a. 用户A拥有角色R，角色R拥有权限P。断言用户A拥有权限P。
        b. 从角色R中移除权限P。
        c. **立即** 再次检查，断言用户A **不再拥有** 权限P。
    2.  运行 `mvn verify` 确保所有测试通过。

---

### **阶段一：P1 - 核心架构重构**

此阶段的目标是纠正模块职责混乱的问题，恢复清晰的架构分层。

#### **任务 3：迁移核心实体类 (`Role`, `Permission`)**
*   **问题**: `Role` 和 `Permission` 实体位于 `promanage-service` 模块，而 `User` 在 `promanage-common`。
*   **目标**: 将所有核心实体统一到 `promanage-common` 模块。
*   **原子化步骤**:
    1.  将 `Role.java` 和 `Permission.java` 文件从 `promanage-service` 移动到 `promanage-common` 的 `entity` 包下。
    2.  修改这两个文件的 `package` 声明。
    3.  在整个项目中搜索并替换对旧包的引用。
*   **验证**:
    1.  执行 `mvn clean install`，确保项目可以成功编译和打包。

#### **任务 4：迁移并整合 DTO**
*   **问题**: `promanage-service` 模块中存在一个 `dto` 包。
*   **目标**: 将所有 DTO 统一到 `promanage-dto` 模块。
*   **原子化步骤**:
    1.  将 `promanage-service` 下 `dto` 包内的所有 Java 文件移动到 `promanage-dto` 模块的相应包下。
    2.  修改这些文件的 `package` 声明。
    3.  在整个项目中搜索并替换对旧包的引用。
    4.  删除 `promanage-service` 下的空 `dto` 目录。
*   **验证**:
    1.  执行 `mvn clean install`。

#### **任务 5：迁移基础设施相关代码**
*   **问题**: `security`, `mapper`, `config` 等包位于 `promanage-service` 模块。
*   **目标**: 将所有基础设施相关的代码统一到 `promanage-infrastructure` 模块。
*   **原子化步骤**:
    1.  **迁移 `PermissionCheckService`**: 将该文件移动到 `infrastructure` 模块的 `security` 包下，并更新引用。
    2.  **迁移 `mapper`**: 将整个 `mapper` 包移动到 `infrastructure` 模块下，并更新 Mybatis 的 Mapper 扫描路径配置。
    3.  **迁移 `config`**: 将 `service` 模块下的 `config` 包移动到 `infrastructure` 模块下。
*   **验证**:
    1.  针对每个子步骤，执行 `mvn clean install` 确保编译通过。
    2.  全部迁移完成后，执行 `mvn verify` 确保所有测试通过。

#### **任务 6：统一授权逻辑 (试点)**
*   **问题**: 权限检查散落在业务代码的 `if` 判断中。
*   **目标**: 将一个方法的权限检查逻辑迁移到 Controller 层的 `@PreAuthorize` 注解。
*   **原子化步骤**:
    1.  选择一个目标：`UserController` 中的 `delete` 方法。
    2.  在 `UserController` 的对应方法上添加注解：`@PreAuthorize("@permissionCheck.isSuperAdmin(authentication)")`。
    3.  移除 `UserServiceImpl.delete` 方法内部的 `if (!permissionService.isSuperAdmin(...))` 检查。
*   **验证**:
    1.  为 `UserController.delete` 编写集成测试，分别使用管理员和非管理员 Token 调用该接口，断言前者成功，后者返回 403 Forbidden。
    2.  运行 `mvn verify`。

---

### **阶段二：P2 - 代码质量与技术债务清理**

此阶段处理剩余的非紧急问题。

#### **任务 7：拆分 `UserServiceImpl`**
*   **目标**: 将 `UserDetailsService` 的实现从 `UserServiceImpl` 中分离。
*   **原子化步骤**:
    1.  创建一个新的类 `UserDetailsServiceImpl`，实现 `UserDetailsService` 接口。
    2.  将 `loadUserByUsername` 方法从 `UserServiceImpl` 移动到 `UserDetailsServiceImpl`。
    3.  让 `UserServiceImpl` 不再实现 `UserDetailsService` 接口。
*   **验证**:
    1.  修改 Spring Security 配置，使用新的 `UserDetailsServiceImpl` 作为 `UserDetailsService`。
    2.  运行所有与登录和认证相关的集成测试。
    3.  运行 `mvn verify`。

#### **任务 8：收紧 PMD 静态检查规则**
*   **目标**: 逐步恢复被禁用的 PMD 规则，并修复因此产生的问题。
*   **原子化步骤**:
    1.  在 `pmd-ruleset.xml` 中，首先恢复 `AvoidDuplicateLiterals` 规则。
    2.  运行 `mvn pmd:check`，找到所有违反该规则的地方。
    3.  将重复的“魔法值”提取为常量。
*   **验证**:
    1.  `mvn pmd:check` 不再报告 `AvoidDuplicateLiterals` 错误。
    2.  `mvn verify` 通过。
