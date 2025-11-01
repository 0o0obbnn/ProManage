package com.promanage.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
import com.promanage.domain.entity.UserRole;
import com.promanage.domain.entity.RolePermission;

import lombok.extern.slf4j.Slf4j;

/**
 * Domain模块实体类测试
 * 
 * <p>测试迁移到domain模块的实体类的基本功能和验证
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@DisplayName("Domain实体类测试")
class DomainEntityTest {

    private Permission permission;
    private Role role;
    private UserRole userRole;
    private RolePermission rolePermission;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        permission = new Permission();
        role = new Role();
        userRole = new UserRole();
        rolePermission = new RolePermission();
    }

    @Test
    @DisplayName("Permission实体 - 基本属性设置和获取")
    void testPermission_BasicProperties() {
        // Given
        Long id = 1L;
        String permissionName = "创建文档";
        String permissionCode = "document:create";
        String type = "api";
        String url = "/api/documents";
        String routePath = "/documents";
        Long parentId = 0L;
        Integer sort = 1;
        Integer status = 0;
        LocalDateTime now = LocalDateTime.now();

        // When
        permission.setId(id);
        permission.setPermissionName(permissionName);
        permission.setPermissionCode(permissionCode);
        permission.setType(type);
        permission.setUrl(url);
        permission.setPath(routePath);
        permission.setParentId(parentId);
        permission.setSort(sort);
        permission.setStatus(status);
        permission.setCreateTime(now);
        permission.setUpdateTime(now);

        // Then
        assertEquals(id, permission.getId());
        assertEquals(permissionName, permission.getPermissionName());
        assertEquals(permissionCode, permission.getPermissionCode());
        assertEquals(type, permission.getType());
        assertEquals(url, permission.getUrl());
        assertEquals(routePath, permission.getPath());
        assertEquals(parentId, permission.getParentId());
        assertEquals(sort, permission.getSort());
        assertEquals(status, permission.getStatus());
        assertEquals(now, permission.getCreateTime());
        assertEquals(now, permission.getUpdateTime());
    }

    @Test
    @DisplayName("Role实体 - 基本属性设置和获取")
    void testRole_BasicProperties() {
        // Given
        Long id = 1L;
        String roleName = "项目经理";
        String roleCode = "ROLE_PM";
        String description = "负责项目管理和团队协调";
        Integer sort = 1;
        Integer status = 0;
        LocalDateTime now = LocalDateTime.now();

        // When
        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setDescription(description);
        role.setSort(sort);
        role.setStatus(status);
        role.setCreateTime(now);
        role.setUpdateTime(now);

        // Then
        assertEquals(id, role.getId());
        assertEquals(roleName, role.getRoleName());
        assertEquals(roleCode, role.getRoleCode());
        assertEquals(description, role.getDescription());
        assertEquals(sort, role.getSort());
        assertEquals(status, role.getStatus());
        assertEquals(now, role.getCreateTime());
        assertEquals(now, role.getUpdateTime());
    }

    @Test
    @DisplayName("UserRole实体 - 基本属性设置和获取")
    void testUserRole_BasicProperties() {
        // Given
        Long id = 1L;
        Long userId = 100L;
        Long roleId = 1L;
        LocalDateTime now = LocalDateTime.now();

        // When
        userRole.setId(id);
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreateTime(now);


        // Then
        assertEquals(id, userRole.getId());
        assertEquals(userId, userRole.getUserId());
        assertEquals(roleId, userRole.getRoleId());
        assertEquals(now, userRole.getCreateTime());

    }

    @Test
    @DisplayName("RolePermission实体 - 基本属性设置和获取")
    void testRolePermission_BasicProperties() {
        // Given
        Long id = 1L;
        Long roleId = 1L;
        Long permissionId = 10L;
        LocalDateTime now = LocalDateTime.now();

        // When
        rolePermission.setId(id);
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermission.setCreateTime(now);
        rolePermission.setUpdateTime(now);

        // Then
        assertEquals(id, rolePermission.getId());
        assertEquals(roleId, rolePermission.getRoleId());
        assertEquals(permissionId, rolePermission.getPermissionId());
        assertEquals(now, rolePermission.getCreateTime());
        assertEquals(now, rolePermission.getUpdateTime());
    }

    @Test
    @DisplayName("Permission实体 - 继承BaseEntity功能")
    void testPermission_InheritsBaseEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Long creatorId = 100L;
        Long updaterId = 101L;
        Boolean deleted = false;

        // When
        permission.setCreatorId(creatorId);
        permission.setUpdaterId(updaterId);
        permission.setDeleted(deleted);
        permission.setCreateTime(now);
        permission.setUpdateTime(now);

        // Then
        assertEquals(creatorId, permission.getCreatorId());
        assertEquals(updaterId, permission.getUpdaterId());
        assertEquals(deleted, permission.getDeleted());
        assertEquals(now, permission.getCreateTime());
        assertEquals(now, permission.getUpdateTime());
    }

    @Test
    @DisplayName("Role实体 - 继承BaseEntity功能")
    void testRole_InheritsBaseEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Long creatorId = 100L;
        Long updaterId = 101L;
        Boolean deleted = false;

        // When
        role.setCreatorId(creatorId);
        role.setUpdaterId(updaterId);
        role.setDeleted(deleted);
        role.setCreateTime(now);
        role.setUpdateTime(now);

        // Then
        assertEquals(creatorId, role.getCreatorId());
        assertEquals(updaterId, role.getUpdaterId());
        assertEquals(deleted, role.getDeleted());
        assertEquals(now, role.getCreateTime());
        assertEquals(now, role.getUpdateTime());
    }

    @Test
    @DisplayName("Permission实体 - 权限类型验证")
    void testPermission_TypeValidation() {
        // Given & When & Then
        permission.setType("menu");
        assertEquals("menu", permission.getType());

        permission.setType("button");
        assertEquals("button", permission.getType());

        permission.setType("api");
        assertEquals("api", permission.getType());
    }

    @Test
    @DisplayName("Role实体 - 角色状态验证")
    void testRole_StatusValidation() {
        // Given & When & Then
        role.setStatus(0);
        assertEquals(0, role.getStatus());

        role.setStatus(1);
        assertEquals(1, role.getStatus());
    }

    @Test
    @DisplayName("实体对象 - toString方法测试")
    void testEntity_ToString() {
        // Given
        permission.setId(1L);
        permission.setPermissionName("测试权限");
        permission.setPermissionCode("test:permission");

        role.setId(1L);
        role.setRoleName("测试角色");
        role.setRoleCode("ROLE_TEST");

        // When
        String permissionStr = permission.toString();
        String roleStr = role.toString();

        // Then
        assertNotNull(permissionStr);
        assertNotNull(roleStr);
        assertTrue(permissionStr.contains("Permission"));
        assertTrue(roleStr.contains("Role"));
    }

    @Test
    @DisplayName("实体对象 - equals和hashCode方法测试")
    void testEntity_EqualsAndHashCode() {
        // Given
        Permission permission1 = new Permission();
        permission1.setId(1L);
        permission1.setPermissionCode("test:permission");

        Permission permission2 = new Permission();
        permission2.setId(1L);
        permission2.setPermissionCode("test:permission");

        Permission permission3 = new Permission();
        permission3.setId(2L);
        permission3.setPermissionCode("test:permission");

        // When & Then
        assertEquals(permission1, permission2);
        assertNotEquals(permission1, permission3);
        assertEquals(permission1.hashCode(), permission2.hashCode());
        assertNotEquals(permission1.hashCode(), permission3.hashCode());
    }

    @Test
    @DisplayName("实体对象 - 可序列化验证")
    void testEntity_IsSerializable() {
        // Given & When & Then
        assertTrue(java.io.Serializable.class.isAssignableFrom(Permission.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(Role.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(UserRole.class));
        assertTrue(java.io.Serializable.class.isAssignableFrom(RolePermission.class));
    }
}
