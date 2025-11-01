package com.promanage.service.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.promanage.common.entity.User;
import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
import com.promanage.domain.entity.UserRole;
import com.promanage.domain.entity.RolePermission;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试数据构建器
 * 
 * <p>提供统一的测试数据创建方法，简化测试用例编写
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
public class TestDataBuilder {

    private static final LocalDateTime DEFAULT_CREATE_TIME = LocalDateTime.now();
    private static final LocalDateTime DEFAULT_UPDATE_TIME = LocalDateTime.now();

    /**
     * 创建测试用户
     */
    public static User createUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone("13800138000");
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // "password"
        user.setStatus(0);
        user.setDeleted(false);
        user.setCreateTime(DEFAULT_CREATE_TIME);
        user.setUpdateTime(DEFAULT_UPDATE_TIME);
        user.setCreatorId(1L);
        user.setUpdaterId(1L);
        return user;
    }

    /**
     * 创建测试角色
     */
    public static Role createRole(Long id, String roleName, String roleCode) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDescription("测试角色描述");
        role.setSort(1);
        role.setStatus(0);
        role.setDeleted(false);
        role.setCreateTime(DEFAULT_CREATE_TIME);
        role.setUpdateTime(DEFAULT_UPDATE_TIME);
        role.setCreatorId(1L);
        role.setUpdaterId(1L);
        return role;
    }

    /**
     * 创建测试权限
     */
    public static Permission createPermission(Long id, String permissionName, String permissionCode, String type) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setPermissionName(permissionName);
        permission.setPermissionCode(permissionCode);
        permission.setType(type);
        permission.setUrl("/api/" + permissionCode.replace(":", "/"));
        permission.setPath("/" + permissionCode.split(":")[0]);
        permission.setParentId(0L);
        permission.setSort(1);
        permission.setStatus(0);
        permission.setDeleted(false);
        permission.setCreateTime(DEFAULT_CREATE_TIME);
        permission.setUpdateTime(DEFAULT_UPDATE_TIME);
        permission.setCreatorId(1L);
        permission.setUpdaterId(1L);
        return permission;
    }

    /**
     * 创建用户角色关联
     */
    public static UserRole createUserRole(Long id, Long userId, Long roleId) {
        UserRole userRole = new UserRole();
        userRole.setId(id);
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreateTime(DEFAULT_CREATE_TIME);
        return userRole;
    }

    /**
     * 创建角色权限关联
     */
    public static RolePermission createRolePermission(Long id, Long roleId, Long permissionId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setId(id);
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermission.setCreateTime(DEFAULT_CREATE_TIME);
        rolePermission.setUpdateTime(DEFAULT_UPDATE_TIME);
        return rolePermission;
    }

    /**
     * 创建默认测试用户列表
     */
    public static List<User> createDefaultUsers() {
        return Arrays.asList(
            createUser(100L, "admin", "admin@example.com"),
            createUser(101L, "pm1", "pm1@example.com"),
            createUser(102L, "dev1", "dev1@example.com"),
            createUser(103L, "test1", "test1@example.com")
        );
    }

    /**
     * 创建默认测试角色列表
     */
    public static List<Role> createDefaultRoles() {
        return Arrays.asList(
            createRole(1L, "系统管理员", "ROLE_ADMIN"),
            createRole(2L, "项目经理", "ROLE_PM"),
            createRole(3L, "开发人员", "ROLE_DEVELOPER"),
            createRole(4L, "测试人员", "ROLE_TESTER")
        );
    }

    /**
     * 创建默认测试权限列表
     */
    public static List<Permission> createDefaultPermissions() {
        return Arrays.asList(
            createPermission(10L, "创建文档", "document:create", "api"),
            createPermission(11L, "编辑文档", "document:edit", "api"),
            createPermission(12L, "删除文档", "document:delete", "api"),
            createPermission(13L, "查看文档", "document:view", "api"),
            createPermission(14L, "创建项目", "project:create", "api"),
            createPermission(15L, "编辑项目", "project:edit", "api"),
            createPermission(16L, "删除项目", "project:delete", "api"),
            createPermission(17L, "查看项目", "project:view", "api"),
            createPermission(18L, "用户管理", "user:manage", "api"),
            createPermission(19L, "角色管理", "role:manage", "api")
        );
    }

    /**
     * 创建用户角色关联列表
     */
    public static List<UserRole> createDefaultUserRoles() {
        return Arrays.asList(
            createUserRole(1L, 100L, 1L), // admin -> ROLE_ADMIN
            createUserRole(2L, 101L, 2L), // pm1 -> ROLE_PM
            createUserRole(3L, 102L, 3L), // dev1 -> ROLE_DEVELOPER
            createUserRole(4L, 103L, 4L)  // test1 -> ROLE_TESTER
        );
    }

    /**
     * 创建角色权限关联列表
     */
    public static List<RolePermission> createDefaultRolePermissions() {
        return Arrays.asList(
            // ROLE_ADMIN 拥有所有权限
            createRolePermission(1L, 1L, 10L), // document:create
            createRolePermission(2L, 1L, 11L), // document:edit
            createRolePermission(3L, 1L, 12L), // document:delete
            createRolePermission(4L, 1L, 13L), // document:view
            createRolePermission(5L, 1L, 14L), // project:create
            createRolePermission(6L, 1L, 15L), // project:edit
            createRolePermission(7L, 1L, 16L), // project:delete
            createRolePermission(8L, 1L, 17L), // project:view
            createRolePermission(9L, 1L, 18L), // user:manage
            createRolePermission(10L, 1L, 19L), // role:manage
            
            // ROLE_PM 拥有项目相关权限
            createRolePermission(11L, 2L, 13L), // document:view
            createRolePermission(12L, 2L, 14L), // project:create
            createRolePermission(13L, 2L, 15L), // project:edit
            createRolePermission(14L, 2L, 17L), // project:view
            
            // ROLE_DEVELOPER 拥有文档相关权限
            createRolePermission(15L, 3L, 10L), // document:create
            createRolePermission(16L, 3L, 11L), // document:edit
            createRolePermission(17L, 3L, 13L), // document:view
            createRolePermission(18L, 3L, 17L), // project:view
            
            // ROLE_TESTER 拥有查看权限
            createRolePermission(19L, 4L, 13L), // document:view
            createRolePermission(20L, 4L, 17L)  // project:view
        );
    }

    /**
     * 创建指定数量的用户ID列表
     */
    public static List<Long> createUserIds(int count) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            userIds.add((long) (i + 1));
        }
        return userIds;
    }

    /**
     * 创建指定数量的角色ID列表
     */
    public static List<Long> createRoleIds(int count) {
        List<Long> roleIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            roleIds.add((long) (i + 1));
        }
        return roleIds;
    }

    /**
     * 创建指定数量的权限ID列表
     */
    public static List<Long> createPermissionIds(int count) {
        List<Long> permissionIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            permissionIds.add((long) (i + 10)); // 从10开始，避免与角色ID冲突
        }
        return permissionIds;
    }

    /**
     * 创建测试数据统计信息
     */
    public static TestDataStats createTestDataStats() {
        return TestDataStats.builder()
            .userCount(4)
            .roleCount(4)
            .permissionCount(10)
            .userRoleCount(4)
            .rolePermissionCount(20)
            .build();
    }

    /**
     * 测试数据统计信息
     */
    public static class TestDataStats {
        private int userCount;
        private int roleCount;
        private int permissionCount;
        private int userRoleCount;
        private int rolePermissionCount;

        public static TestDataStatsBuilder builder() {
            return new TestDataStatsBuilder();
        }

        public static class TestDataStatsBuilder {
            private TestDataStats stats = new TestDataStats();

            public TestDataStatsBuilder userCount(int userCount) {
                stats.userCount = userCount;
                return this;
            }

            public TestDataStatsBuilder roleCount(int roleCount) {
                stats.roleCount = roleCount;
                return this;
            }

            public TestDataStatsBuilder permissionCount(int permissionCount) {
                stats.permissionCount = permissionCount;
                return this;
            }

            public TestDataStatsBuilder userRoleCount(int userRoleCount) {
                stats.userRoleCount = userRoleCount;
                return this;
            }

            public TestDataStatsBuilder rolePermissionCount(int rolePermissionCount) {
                stats.rolePermissionCount = rolePermissionCount;
                return this;
            }

            public TestDataStats build() {
                return stats;
            }
        }

        // Getters
        public int getUserCount() { return userCount; }
        public int getRoleCount() { return roleCount; }
        public int getPermissionCount() { return permissionCount; }
        public int getUserRoleCount() { return userRoleCount; }
        public int getRolePermissionCount() { return rolePermissionCount; }
    }
}
