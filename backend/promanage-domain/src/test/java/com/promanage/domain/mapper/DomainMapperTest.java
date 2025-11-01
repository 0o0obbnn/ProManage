package com.promanage.domain.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.entity.User;
import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
import lombok.extern.slf4j.Slf4j;

/**
 * Domain模块Mapper接口测试
 * 
 * <p>测试迁移到domain模块的Mapper接口的基本功能
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Domain Mapper接口测试")
class DomainMapperTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    private User testUser;
    private Role testRole;
    private Permission testPermission;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");

        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("测试角色");
        testRole.setRoleCode("ROLE_TEST");
        testRole.setStatus(0);

        testPermission = new Permission();
        testPermission.setId(10L);
        testPermission.setPermissionName("测试权限");
        testPermission.setPermissionCode("test:permission");
        testPermission.setType("api");
    }

    @Test
    @DisplayName("UserMapper - 根据用户名查询用户")
    void testUserMapper_FindByUsername() {
        // Given
        String username = "testuser";
        when(userMapper.findByUsername(username)).thenReturn(testUser);

        // When
        User result = userMapper.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userMapper).findByUsername(username);
    }

    @Test
    @DisplayName("UserMapper - 根据邮箱查询用户")
    void testUserMapper_FindByEmail() {
        // Given
        String email = "test@example.com";
        when(userMapper.findByEmail(email)).thenReturn(testUser);

        // When
        User result = userMapper.findByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userMapper).findByEmail(email);
    }

    @Test
    @DisplayName("UserMapper - 检查用户名是否存在")
    void testUserMapper_ExistsByUsername() {
        // Given
        String username = "testuser";
        when(userMapper.existsByUsername(username)).thenReturn(1);

        // When
        int result = userMapper.existsByUsername(username);

        // Then
        assertEquals(1, result);
        verify(userMapper).existsByUsername(username);
    }

    @Test
    @DisplayName("RoleMapper - 根据用户ID查找角色列表")
    void testRoleMapper_FindByUserId() {
        // Given
        Long userId = 100L;
        List<Role> roles = Arrays.asList(testRole);
        when(roleMapper.findByUserId(userId)).thenReturn(roles);

        // When
        List<Role> result = roleMapper.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRole.getId(), result.get(0).getId());
        verify(roleMapper).findByUserId(userId);
    }

    @Test
    @DisplayName("RoleMapper - 根据角色编码查找角色")
    void testRoleMapper_FindByRoleCode() {
        // Given
        String roleCode = "ROLE_TEST";
        when(roleMapper.findByRoleCode(roleCode)).thenReturn(testRole);

        // When
        Role result = roleMapper.findByRoleCode(roleCode);

        // Then
        assertNotNull(result);
        assertEquals(roleCode, result.getRoleCode());
        verify(roleMapper).findByRoleCode(roleCode);
    }

    @Test
    @DisplayName("RoleMapper - 检查角色编码是否存在")
    void testRoleMapper_ExistsByRoleCode() {
        // Given
        String roleCode = "ROLE_TEST";
        when(roleMapper.existsByRoleCode(roleCode)).thenReturn(true);

        // When
        boolean result = roleMapper.existsByRoleCode(roleCode);

        // Then
        assertTrue(result);
        verify(roleMapper).existsByRoleCode(roleCode);
    }

    @Test
    @DisplayName("PermissionMapper - 根据角色ID查找权限列表")
    void testPermissionMapper_FindByRoleId() {
        // Given
        Long roleId = 1L;
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionMapper.findByRoleId(roleId)).thenReturn(permissions);

        // When
        List<Permission> result = permissionMapper.findByRoleId(roleId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermission.getId(), result.get(0).getId());
        verify(permissionMapper).findByRoleId(roleId);
    }

    @Test
    @DisplayName("PermissionMapper - 根据权限编码查找权限")
    void testPermissionMapper_FindByPermissionCode() {
        // Given
        String permissionCode = "test:permission";
        when(permissionMapper.findByPermissionCode(permissionCode)).thenReturn(testPermission);

        // When
        Permission result = permissionMapper.findByPermissionCode(permissionCode);

        // Then
        assertNotNull(result);
        assertEquals(permissionCode, result.getPermissionCode());
        verify(permissionMapper).findByPermissionCode(permissionCode);
    }

    @Test
    @DisplayName("UserRoleMapper - 根据角色ID查找用户ID列表")
    void testUserRoleMapper_FindUserIdsByRoleId() {
        // Given
        Long roleId = 1L;
        List<Long> userIds = Arrays.asList(100L, 101L, 102L);
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);

        // When
        List<Long> result = userRoleMapper.findUserIdsByRoleId(roleId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(100L));
        assertTrue(result.contains(101L));
        assertTrue(result.contains(102L));
        verify(userRoleMapper).findUserIdsByRoleId(roleId);
    }

    @Test
    @DisplayName("UserRoleMapper - 根据用户ID查找角色ID列表")
    void testUserRoleMapper_FindRoleIdsByUserId() {
        // Given
        Long userId = 100L;
        List<Long> roleIds = Arrays.asList(1L, 2L);
        when(userRoleMapper.selectRoleIdsByUserId(userId)).thenReturn(roleIds);

        // When
        List<Long> result = userRoleMapper.selectRoleIdsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
        verify(userRoleMapper).selectRoleIdsByUserId(userId);
    }

    @Test
    @DisplayName("RolePermissionMapper - 根据角色ID查找权限ID列表")
    void testRolePermissionMapper_FindPermissionIdsByRoleId() {
        // Given
        Long roleId = 1L;
        List<Long> permissionIds = Arrays.asList(10L, 11L, 12L);
        when(rolePermissionMapper.selectPermissionIdsByRoleId(roleId)).thenReturn(permissionIds);

        // When
        List<Long> result = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(10L));
        assertTrue(result.contains(11L));
        assertTrue(result.contains(12L));
        verify(rolePermissionMapper).selectPermissionIdsByRoleId(roleId);
    }

    @Test
    @DisplayName("UserRoleMapper - 空结果处理")
    void testUserRoleMapper_EmptyResult() {
        // Given
        Long roleId = 999L;
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(Collections.emptyList());

        // When
        List<Long> result = userRoleMapper.findUserIdsByRoleId(roleId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRoleMapper).findUserIdsByRoleId(roleId);
    }

    @Test
    @DisplayName("Mapper接口 - 继承BaseMapper功能")
    void testMapper_InheritsBaseMapper() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        
        when(userMapper.insert(newUser)).thenReturn(1);

        // When
        int result = userMapper.insert(newUser);

        // Then
        assertEquals(1, result);
        verify(userMapper).insert(newUser);
    }

    @Test
    @DisplayName("Mapper接口 - 分页查询功能")
    @SuppressWarnings("unchecked")
    void testMapper_PageQuery() {
        // Given
        Page<Role> page = new Page<>(1, 10);
        Page<Role> pageResult = new Page<>();
        pageResult.setRecords(Arrays.asList(testRole));
        pageResult.setTotal(1);
        
        when(roleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(pageResult);

        // When
        IPage<Role> result = roleMapper.selectPage(page, new LambdaQueryWrapper<Role>());

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(roleMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("Mapper接口 - 条件查询功能")
    @SuppressWarnings("unchecked")
    void testMapper_ConditionalQuery() {
        // Given
        String roleCode = "ROLE_TEST";
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRole);

        // When
        Role result = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
            .eq(Role::getRoleCode, roleCode));

        // Then
        assertNotNull(result);
        assertEquals(roleCode, result.getRoleCode());
        verify(roleMapper).selectOne(any(LambdaQueryWrapper.class));
    }
}
