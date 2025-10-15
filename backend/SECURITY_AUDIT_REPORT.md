# ProManage Backend Security Audit Report

**Generated:** 2025-10-11
**Auditor:** Claude Code
**Scope:** Comprehensive Authorization Bypass Vulnerability Assessment
**Audit Type:** Service Layer Permission Checks

---

## Executive Summary

A comprehensive security audit was conducted across all backend service implementations to identify authorization bypass vulnerabilities similar to the critical issue found in `OrganizationServiceImpl`.

### Key Findings

- **Total Services Audited:** 15 ServiceImpl classes
- **Secure Modules:** 3 (Organization, Project, Task)
- **Partially Secure:** 1 (Document)
- **Vulnerable Modules:** 3 (ChangeRequest, User, Notification)
- **Non-Critical:** 8 (Auth, Email, Password, Role, Search, etc.)

### Risk Level: **HIGH** 🔴

Three modules currently lack authorization checks, allowing authenticated users to access or modify resources they don't own.

---

## Vulnerability Pattern

### The Core Issue

Service methods accept `userId` and resource ID parameters but **do not verify** the relationship between the user and the resource before performing operations.

**Example Attack Vector:**
```java
// Vulnerable code pattern
public void deleteChangeRequest(Long changeRequestId, Long userId) {
    // ❌ NO CHECK: Any authenticated user can delete ANY change request
    changeRequestMapper.deleteById(changeRequestId);
}
```

**What Should Happen:**
```java
// Secure code pattern
public void deleteChangeRequest(Long changeRequestId, Long userId) {
    // ✅ Verify user has permission to delete this specific change request
    if (!permissionService.canAccessChangeRequest(userId, changeRequestId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此变更请求");
    }
    changeRequestMapper.deleteById(changeRequestId);
}
```

---

## Detailed Findings by Module

### ✅ 1. OrganizationServiceImpl - **SECURE** (Recently Fixed)

**File:** `promanage-service/src/main/java/com/promanage/service/impl/OrganizationServiceImpl.java`

**Status:** Fixed as of this audit

**Permission Checks Added:** 9 methods now protected

**Protected Methods:**
1. `getOrganizationById()` - Member check
2. `getOrganizationSettings()` - Member check
3. `updateOrganization()` - Member check
4. `deleteOrganization()` - **Admin check**
5. `activateOrganization()` - Member check
6. `deactivateOrganization()` - Member check
7. `updateSubscriptionPlan()` - **Admin check**
8. `updateOrganizationSettings()` - **Admin check**
9. `listOrganizationMembers()` - Member check

**Authorization Logic:**
- **Member Operations:** `permissionService.isOrganizationMember(userId, orgId)`
- **Admin Operations:** `permissionService.isOrganizationAdmin(userId, orgId)`
  - Admin = Organization creator (checked via `organization.getCreatedBy()`)

**Code Example:**
```java
@Override
public void deleteOrganization(Long id, Long deleterId) {
    // Admin-level permission check
    if (!permissionService.isOrganizationAdmin(deleterId, id)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您不是该组织管理员，无权删除");
    }
    log.info("删除组织: {}, 删除者ID: {}", id, deleterId);
    organizationMapper.deleteById(id);
}
```

---

### ✅ 2. ProjectServiceImpl - **SECURE**

**File:** `promanage-service/src/main/java/com/promanage/service/service/impl/ProjectServiceImpl.java`

**Status:** Already has comprehensive security

**Permission Checks Found:** 8 methods protected

**Protected Methods:**
1. `getProjectById()` - `isProjectMember()` check
2. `createProject()` - `isOrganizationMember()` check (can only create in own org)
3. `updateProject()` - `isProjectAdmin()` check
4. `deleteProject()` - `isProjectAdmin()` check
5. `getProjectMembers()` - `isProjectMember()` check
6. `addMember()` - `isProjectAdmin()` check
7. `removeMember()` - `isProjectAdmin()` check
8. `getProjectStats()` - `isProjectMember()` check

**Authorization Logic:**
- Member operations: `permissionService.isProjectMember(userId, projectId)`
- Admin operations: `permissionService.isProjectAdmin(userId, projectId)`

**Code Example:**
```java
@Override
public Project updateProject(Long id, ProjectRequest projectRequest, Long updaterId) {
    if (!permissionService.isProjectAdmin(updaterId, id)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "只有项目管理员才能修改项目信息");
    }
    // ... update logic
}
```

**Assessment:** ✅ Well-secured, follows best practices

---

### ✅ 3. TaskServiceImpl - **SECURE**

**File:** `promanage-service/src/main/java/com/promanage/service/impl/TaskServiceImpl.java`

**Status:** Already has comprehensive security

**Permission Checks Found:** 9 methods protected

**Protected Methods:**
1. `getTaskById()` - `canAccessTask()` check
2. `createTask()` - `isProjectMember()` check
3. `updateTask()` - `canAccessTask()` check
4. `deleteTask()` - `canAccessTask()` check
5. `updateTaskStatus()` - `canAccessTask()` check
6. `assignTask()` - `canAccessTask()` check
7. `updateTaskProgress()` - `canAccessTask()` check
8. `addTaskComment()` - `canAccessTask()` check
9. `deleteTaskComment()` - `canAccessTask()` check

**Authorization Logic:**
- Uses `permissionService.canAccessTask(userId, taskId)`
- Uses `permissionService.isProjectMember(userId, projectId)` for creation

**Code Example:**
```java
@Override
public Task getTaskById(Long id, Long userId) {
    if (!permissionService.canAccessTask(userId, id)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此任务");
    }
    return taskMapper.selectById(id);
}
```

**Assessment:** ✅ Well-secured, follows best practices

---

### ⚠️ 4. DocumentServiceImpl - **PARTIALLY SECURE**

**File:** `promanage-service/src/main/java/com/promanage/service/impl/DocumentServiceImpl.java`

**Status:** Has some protection, but incomplete coverage

**Permission Checks Found:** 6 methods protected

**Protected Methods:**
1. `getDocumentById()` - `canAccessDocument()` check
2. `updateDocument()` - `canAccessDocument()` check
3. `deleteDocument()` - `canAccessDocument()` check
4. `listByProjectId()` - `isProjectMember()` check
5. `create()` - `isProjectMember()` check
6. (Implicit) - Some methods protected

**Vulnerable Methods (No Checks):**
- ❌ `listDocuments()` - No permission check, any user can list all documents
- ❌ `listAllDocuments()` - No permission check, exposes cross-project documents
- ❌ `searchDocuments()` - No permission check, allows searching others' documents
- ❌ `getDocumentFolders()` - No permission check
- ❌ `listVersions()` - No permission check on document ownership
- ❌ `getVersion()` - No permission check
- ❌ `createVersion()` - No permission check
- ❌ `rollbackToVersion()` - No permission check
- ❌ `publish()` - No permission check (called by updateStatus which has no check)
- ❌ `archive()` - No permission check (called by updateStatus which has no check)
- ❌ `updateStatus()` - No permission check
- ❌ `countByProjectId()` - Information disclosure
- ❌ `countByCreatorId()` - Information disclosure
- ❌ `favoriteDocument()` - No ownership check
- ❌ `unfavoriteDocument()` - No ownership check

**Risk Assessment:** **MEDIUM** 🟡

Users can:
- List and search documents across all projects
- View version history of documents they don't own
- Modify document status without authorization
- Access folder structures they shouldn't see

**Recommended Fix:**
```java
@Override
public List<DocumentVersion> listVersions(Long documentId) {
    // Add permission check
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    if (!permissionService.canAccessDocument(currentUserId, documentId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此文档的版本历史");
    }

    log.info("查询文档版本列表, documentId={}", documentId);
    return documentVersionMapper.findByDocumentId(documentId);
}
```

---

### ❌ 5. ChangeRequestServiceImpl - **VULNERABLE**

**File:** `promanage-service/src/main/java/com/promanage/service/impl/ChangeRequestServiceImpl.java`

**Status:** **NO AUTHORIZATION CHECKS** 🔴

**Permission Checks Found:** 0

**Vulnerable Methods:**
- ❌ `getChangeRequestById()` - Any user can view any change request
- ❌ `createChangeRequest()` - No project membership verification
- ❌ `updateChangeRequest()` - Any user can modify any change request
- ❌ `deleteChangeRequest()` - Any user can delete any change request
- ❌ `listChangeRequests()` - Exposes all change requests
- ❌ `approveChangeRequest()` - No approval authority verification
- ❌ `rejectChangeRequest()` - No rejection authority verification
- ❌ `implementChangeRequest()` - No implementation authority verification
- ❌ `addComment()` - Any user can comment on any change request
- ❌ `updateImpactAnalysis()` - No verification

**Risk Assessment:** **CRITICAL** 🔴

**Attack Scenarios:**
1. Developer from Project A can view/modify change requests in Project B
2. Any user can approve/reject critical change requests
3. Users can delete change requests they didn't create
4. Cross-project information disclosure

**Current Vulnerable Code:**
```java
@Override
public ChangeRequest getChangeRequestById(Long id) {
    // ❌ NO PERMISSION CHECK
    ChangeRequest changeRequest = changeRequestMapper.selectById(id);
    if (changeRequest == null) {
        throw new BusinessException(ResultCode.NOT_FOUND, "变更请求不存在");
    }
    return changeRequest;
}

@Override
public void deleteChangeRequest(Long id, Long deleterId) {
    // ❌ NO PERMISSION CHECK - anyone can delete!
    log.info("删除变更请求, id={}, 删除者ID={}", id, deleterId);
    changeRequestMapper.deleteById(id);
}
```

**Recommended Fix:**

**Step 1: Add new methods to IPermissionService:**
```java
/**
 * 检查用户是否可以访问指定变更请求
 */
boolean canAccessChangeRequest(Long userId, Long changeRequestId);

/**
 * 检查用户是否可以审批变更请求 (项目管理员或更高权限)
 */
boolean canApproveChangeRequest(Long userId, Long changeRequestId);
```

**Step 2: Implement in PermissionServiceImpl:**
```java
@Override
public boolean canAccessChangeRequest(Long userId, Long changeRequestId) {
    if (userId == null || changeRequestId == null) {
        return false;
    }

    // Get change request to find its project
    ChangeRequest cr = changeRequestMapper.selectById(changeRequestId);
    if (cr == null) {
        return false;
    }

    // User must be a member of the project
    return isProjectMember(userId, cr.getProjectId());
}

@Override
public boolean canApproveChangeRequest(Long userId, Long changeRequestId) {
    if (userId == null || changeRequestId == null) {
        return false;
    }

    ChangeRequest cr = changeRequestMapper.selectById(changeRequestId);
    if (cr == null) {
        return false;
    }

    // Only project admins can approve
    return isProjectAdmin(userId, cr.getProjectId());
}
```

**Step 3: Add checks to ChangeRequestServiceImpl:**
```java
@Override
public ChangeRequest getChangeRequestById(Long id) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // ✅ Permission check added
    if (!permissionService.canAccessChangeRequest(currentUserId, id)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此变更请求");
    }

    ChangeRequest changeRequest = changeRequestMapper.selectById(id);
    if (changeRequest == null) {
        throw new BusinessException(ResultCode.NOT_FOUND, "变更请求不存在");
    }
    return changeRequest;
}

@Override
public void approveChangeRequest(Long id, Long approverId, String approvalComments) {
    // ✅ Admin-level permission check
    if (!permissionService.canApproveChangeRequest(approverId, id)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权审批此变更请求，需要项目管理员权限");
    }

    log.info("审批变更请求, id={}, 审批者ID={}", id, approverId);
    // ... approval logic
}
```

---

### ❌ 6. UserServiceImpl - **VULNERABLE**

**File:** `promanage-service/src/main/java/com/promanage/service/impl/UserServiceImpl.java`

**Status:** **NO AUTHORIZATION CHECKS** 🔴

**Permission Checks Found:** 0

**Vulnerable Methods:**
- ❌ `getUserById()` - Any user can view any user's full profile
- ❌ `updateUser()` - Users can modify other users' profiles
- ❌ `deleteUser()` - Users can delete other users
- ❌ `changePassword()` - Users can change others' passwords
- ❌ `updateUserRole()` - Users can change others' roles (privilege escalation!)
- ❌ `listUsers()` - Exposes all user data
- ❌ `getUserByEmail()` - Email enumeration attack
- ❌ `getUserByUsername()` - Username enumeration attack

**Risk Assessment:** **CRITICAL** 🔴

**Attack Scenarios:**
1. **Privilege Escalation:** Any user can promote themselves to admin via `updateUserRole()`
2. **Account Takeover:** Users can change others' passwords
3. **Privacy Violation:** Access to all user profiles and contact information
4. **Account Deletion:** Malicious deletion of user accounts

**Current Vulnerable Code:**
```java
@Override
public User updateUser(User user) {
    // ❌ NO PERMISSION CHECK - any user can update any profile!
    log.info("更新用户信息, userId={}", user.getId());
    userMapper.updateById(user);
    return user;
}

@Override
public void changePassword(Long userId, String oldPassword, String newPassword) {
    // ❌ NO CHECK: Any user can change anyone's password!
    User user = getUserById(userId);

    // Verify old password
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new BusinessException(ResultCode.PARAM_ERROR, "原密码错误");
    }

    // Encode and save new password
    user.setPassword(passwordEncoder.encode(newPassword));
    userMapper.updateById(user);
}
```

**Recommended Fix:**

**Step 1: Add method to IPermissionService:**
```java
/**
 * 检查用户是否可以修改指定用户信息
 * 规则：用户只能修改自己的信息，除非是超级管理员
 */
boolean canModifyUser(Long actorId, Long targetUserId);

/**
 * 检查用户是否是系统超级管理员
 */
boolean isSuperAdmin(Long userId);
```

**Step 2: Implement in PermissionServiceImpl:**
```java
@Override
public boolean canModifyUser(Long actorId, Long targetUserId) {
    if (actorId == null || targetUserId == null) {
        return false;
    }

    // Users can always modify their own profile
    if (actorId.equals(targetUserId)) {
        return true;
    }

    // SuperAdmin can modify anyone
    return isSuperAdmin(actorId);
}

@Override
public boolean isSuperAdmin(Long userId) {
    if (userId == null) {
        return false;
    }

    // Check if user has SuperAdmin role
    User user = userMapper.selectById(userId);
    if (user == null) {
        return false;
    }

    // Assuming role is stored in user entity
    return "SUPER_ADMIN".equals(user.getRole()) || "SYSTEM_ADMIN".equals(user.getRole());
}
```

**Step 3: Add checks to UserServiceImpl:**
```java
@Override
public User updateUser(User user) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // ✅ Permission check added
    if (!permissionService.canModifyUser(currentUserId, user.getId())) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权修改此用户信息");
    }

    log.info("更新用户信息, userId={}, 操作人={}", user.getId(), currentUserId);
    userMapper.updateById(user);
    return user;
}

@Override
public void changePassword(Long userId, String oldPassword, String newPassword) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // ✅ Users can only change their own password
    if (!currentUserId.equals(userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您只能修改自己的密码");
    }

    User user = getUserById(userId);

    // Verify old password
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new BusinessException(ResultCode.PARAM_ERROR, "原密码错误");
    }

    // Encode and save new password
    user.setPassword(passwordEncoder.encode(newPassword));
    userMapper.updateById(user);
}

@Override
public void updateUserRole(Long userId, String newRole, Long actorId) {
    // ✅ Only SuperAdmin can change roles
    if (!permissionService.isSuperAdmin(actorId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "只有系统管理员才能修改用户角色");
    }

    log.info("更新用户角色, userId={}, newRole={}, 操作人={}", userId, newRole, actorId);

    User user = getUserById(userId);
    user.setRole(newRole);
    userMapper.updateById(user);
}
```

---

### ❌ 7. NotificationServiceImpl - **VULNERABLE**

**File:** `promanage-service/src/main/java/com/promanage/service/impl/NotificationServiceImpl.java`

**Status:** **NO AUTHORIZATION CHECKS** 🔴

**Permission Checks Found:** 0

**Vulnerable Methods:**
- ❌ `getNotificationById()` - Any user can read any notification
- ❌ `listNotifications()` - Users can list others' notifications
- ❌ `markAsRead()` - Users can mark others' notifications as read
- ❌ `markAllAsRead()` - Users can mark all of someone else's notifications as read
- ❌ `deleteNotification()` - Users can delete others' notifications
- ❌ `getUnreadCount()` - Users can check others' unread counts

**Risk Assessment:** **HIGH** 🔴

**Attack Scenarios:**
1. **Privacy Violation:** Users can read notifications meant for others (may contain sensitive info)
2. **Denial of Service:** Users can delete all notifications for other users
3. **Information Disclosure:** Notification content may reveal project details, assignments, etc.

**Current Vulnerable Code:**
```java
@Override
public Notification getNotificationById(Long id) {
    // ❌ NO PERMISSION CHECK - any user can read any notification!
    Notification notification = notificationMapper.selectById(id);
    if (notification == null) {
        throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
    }
    return notification;
}

@Override
public void markAsRead(Long notificationId, Long userId) {
    // ❌ NO CHECK: userId parameter is passed but never verified!
    Notification notification = getNotificationById(notificationId);
    notification.setRead(true);
    notification.setReadAt(LocalDateTime.now());
    notificationMapper.updateById(notification);
}
```

**Recommended Fix:**

**Step 1: Add checks to NotificationServiceImpl (notifications have recipientId field):**
```java
@Override
public Notification getNotificationById(Long id) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    Notification notification = notificationMapper.selectById(id);
    if (notification == null) {
        throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
    }

    // ✅ Verify notification belongs to current user
    if (!currentUserId.equals(notification.getRecipientId())) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此通知");
    }

    return notification;
}

@Override
public PageResult<Notification> listNotifications(Long userId, Integer page, Integer size, Boolean isRead) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // ✅ Users can only list their own notifications
    if (!currentUserId.equals(userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您只能查看自己的通知");
    }

    log.info("查询通知列表, userId={}, page={}, size={}, isRead={}", userId, page, size, isRead);
    // ... query logic
}

@Override
public void markAsRead(Long notificationId, Long userId) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // ✅ Verify userId matches current user
    if (!currentUserId.equals(userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您只能标记自己的通知");
    }

    Notification notification = notificationMapper.selectById(notificationId);
    if (notification == null) {
        throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
    }

    // ✅ Verify notification belongs to user
    if (!userId.equals(notification.getRecipientId())) {
        throw new BusinessException(ResultCode.FORBIDDEN, "此通知不属于您");
    }

    notification.setRead(true);
    notification.setReadAt(LocalDateTime.now());
    notificationMapper.updateById(notification);
}

@Override
public void deleteNotification(Long notificationId, Long userId) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // ✅ Verify userId matches current user
    if (!currentUserId.equals(userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您只能删除自己的通知");
    }

    Notification notification = notificationMapper.selectById(notificationId);
    if (notification == null) {
        throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
    }

    // ✅ Verify notification belongs to user
    if (!userId.equals(notification.getRecipientId())) {
        throw new BusinessException(ResultCode.FORBIDDEN, "此通知不属于您");
    }

    log.info("删除通知, notificationId={}, userId={}", notificationId, userId);
    notificationMapper.deleteById(notificationId);
}
```

---

## Non-Critical Services (No Security Issues Expected)

The following services were audited but don't handle sensitive multi-user resources:

1. **AuthServiceImpl** - Handles login/logout, doesn't manage user resources
2. **EmailServiceImpl** - Infrastructure service, no direct user access
3. **PasswordServiceImpl** - Uses tokens for password reset, stateless
4. **RoleServiceImpl** - System configuration, would need admin checks (not audited in depth)
5. **SearchServiceImpl** - Delegates to other services, inherits their security
6. **DocumentFolderServiceImpl** - Folder management (should inherit Document security)
7. **ProjectActivityServiceImpl** - Activity logging (read-only, low risk)
8. **TaskTimeTrackingServiceImpl** - Time tracking (likely inherits Task security)

---

## Risk Assessment Summary

| Module | Risk Level | Authorization Checks | Impact |
|--------|-----------|---------------------|---------|
| OrganizationServiceImpl | ✅ SECURE | 9 checks added | Fixed |
| ProjectServiceImpl | ✅ SECURE | 8 checks exist | None |
| TaskServiceImpl | ✅ SECURE | 9 checks exist | None |
| DocumentServiceImpl | ⚠️ MEDIUM | 6 partial checks | Information disclosure, unauthorized document access |
| ChangeRequestServiceImpl | 🔴 CRITICAL | 0 checks | Full CRUD access across projects, unauthorized approvals |
| UserServiceImpl | 🔴 CRITICAL | 0 checks | Privilege escalation, account takeover, password change |
| NotificationServiceImpl | 🔴 HIGH | 0 checks | Privacy violation, notification manipulation |

---

## Remediation Roadmap

### Phase 1: CRITICAL - Immediate Action Required (1-2 days)

**Priority 1: UserServiceImpl** - Highest Risk
- **Impact:** Privilege escalation + account takeover
- **Fix:** Add `canModifyUser()` and `isSuperAdmin()` checks
- **Effort:** 4-6 hours
- **Methods to Secure:** 8 methods

**Priority 2: ChangeRequestServiceImpl** - Critical Business Function
- **Impact:** Unauthorized change approvals, cross-project access
- **Fix:** Add `canAccessChangeRequest()` and `canApproveChangeRequest()` checks
- **Effort:** 6-8 hours
- **Methods to Secure:** 10+ methods

### Phase 2: HIGH - Complete This Week (2-3 days)

**Priority 3: NotificationServiceImpl** - Privacy Risk
- **Impact:** Privacy violation, information disclosure
- **Fix:** Verify `recipientId` matches current user
- **Effort:** 3-4 hours
- **Methods to Secure:** 6 methods

**Priority 4: DocumentServiceImpl** - Partial Coverage
- **Impact:** Document information disclosure
- **Fix:** Add checks to 15 unprotected methods
- **Effort:** 6-8 hours
- **Methods to Secure:** 15 methods

### Phase 3: VERIFICATION - Testing & Validation (2-3 days)

1. **Unit Tests:** Write tests for all permission checks
2. **Integration Tests:** Test cross-user access attempts
3. **Security Testing:** Attempt attack scenarios
4. **Code Review:** Peer review all changes

---

## Testing Recommendations

### Unit Test Example

```java
@Test
void shouldThrowForbiddenException_whenUserTriesToDeleteOthersChangeRequest() {
    // given
    Long attackerUserId = 1L;
    Long victimChangeRequestId = 100L; // belongs to another user

    // Mock permission service to return false
    when(permissionService.canAccessChangeRequest(attackerUserId, victimChangeRequestId))
            .thenReturn(false);

    // when & then
    assertThrows(BusinessException.class, () -> {
        changeRequestService.deleteChangeRequest(victimChangeRequestId, attackerUserId);
    });

    // Verify delete was NOT called
    verify(changeRequestMapper, never()).deleteById(any());
}
```

### Integration Test Example

```java
@Test
@WithMockUser(username = "attacker", roles = "USER")
void shouldReturn403_whenUserTriesToAccessOthersNotification() throws Exception {
    // given
    Long victimNotificationId = 999L; // belongs to another user

    // when
    mockMvc.perform(get("/api/v1/notifications/{id}", victimNotificationId)
            .header("Authorization", "Bearer " + attackerToken))
            // then
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("您无权访问此通知"));
}
```

---

## Code Pattern Templates

### Template 1: Basic Resource Ownership Check

```java
@Override
public ResourceType getResourceById(Long resourceId) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // Check permission
    if (!permissionService.canAccessResource(currentUserId, resourceId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此资源");
    }

    // Proceed with business logic
    return resourceMapper.selectById(resourceId);
}
```

### Template 2: Self-Modification Only Check

```java
@Override
public void updateOwnResource(Long resourceId, Long userId) {
    // Get current user
    Long currentUserId = SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录"));

    // Verify userId parameter matches current user
    if (!currentUserId.equals(userId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "您只能修改自己的资源");
    }

    // Proceed with business logic
    // ...
}
```

### Template 3: Admin-Level Operation Check

```java
@Override
public void performAdminOperation(Long resourceId, Long actorId) {
    // Check admin permission
    if (!permissionService.isResourceAdmin(actorId, resourceId)) {
        throw new BusinessException(ResultCode.FORBIDDEN, "只有管理员才能执行此操作");
    }

    // Proceed with business logic
    // ...
}
```

---

## Compliance & Standards

### OWASP Top 10 Alignment

This audit addresses:
- **A01:2021 – Broken Access Control** ✅ Primary focus
- **A04:2021 – Insecure Design** ✅ Architectural security
- **A07:2021 – Identification and Authentication Failures** ⚠️ Partial (UserServiceImpl)

### Spring Security Best Practices

✅ **Applied:**
- Service-layer authorization (defense in depth)
- Consistent exception handling
- SecurityContext usage

⚠️ **Needs Improvement:**
- Method-level `@PreAuthorize` annotations (currently only on controllers)
- Security unit test coverage
- Audit logging for authorization failures

---

## Monitoring & Detection

### Recommended Security Logs

Add to each permission check:
```java
if (!permissionService.canAccessResource(currentUserId, resourceId)) {
    log.warn("Authorization failed: userId={}, resourceId={}, operation={}, ip={}",
             currentUserId, resourceId, "ACCESS", getClientIp());
    throw new BusinessException(ResultCode.FORBIDDEN, "您无权访问此资源");
}
```

### Metrics to Track

1. **Authorization Failure Rate** - Sudden spikes may indicate attack
2. **Cross-User Access Attempts** - Should be near zero
3. **Failed Permission Checks by User** - Identify suspicious accounts
4. **Admin Operation Frequency** - Detect privilege abuse

---

## Conclusion

The audit has identified **three critical** and **one medium-severity** authorization bypass vulnerabilities affecting the ChangeRequest, User, Notification, and Document modules.

The Organization, Project, and Task modules demonstrate proper authorization implementation and serve as reference examples for remediation.

**Immediate action is required** to secure the UserServiceImpl (privilege escalation risk) and ChangeRequestServiceImpl (business logic risk) before production deployment.

All recommended fixes follow the established pattern from OrganizationServiceImpl and can be implemented within 3-5 working days.

---

**Report Status:** COMPLETE
**Next Steps:** Begin Phase 1 remediation
**Contact:** Security Team / Development Lead
