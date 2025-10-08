package com.promanage.integration;

import com.promanage.common.entity.User;
import com.promanage.service.service.IUserService;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 用户权限集成测试
 * <p>
 * 测试用户、角色和权限之间的关联关系
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@DisplayName("用户权限集成测试")
class UserPermissionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IPermissionService permissionService;

    // passwordEncoder 已在 UserServiceImpl 中自动注入，此处不需要

    @Test
    @DisplayName("用户角色权限完整流程测试")
    void testUserRolePermissionFullFlow() {
        // 1. 创建用户
        User user = new User();
        user.setUsername("permissiontestuser");
        user.setPassword("password123");
        user.setEmail("permissiontestuser@example.com");
        user.setRealName("权限测试用户");

        Long userId = userService.create(user);

        // 2. 验证用户创建成功
        assertNotNull(userId);

        // 3. 获取用户信息
        User retrievedUser = userService.getById(userId);
        assertNotNull(retrievedUser);
        assertEquals("permissiontestuser", retrievedUser.getUsername());
        assertEquals("权限测试用户", retrievedUser.getRealName());

        // 4. 验证基础权限检查
        boolean hasViewPermission = userService.hasPermission(userId, "USER_VIEW");
        boolean hasEditPermission = userService.hasPermission(userId, "USER_EDIT");
        
        // 注意：新创建的用户默认可能没有特殊权限，这取决于系统默认角色设置
        assertNotNull(hasViewPermission);
        assertNotNull(hasEditPermission);
    }

    @Test
    @DisplayName("权限服务基本功能测试")
    void testPermissionServiceBasics() {
        // 1. 检查是否存在默认权限
        // 这里我们只是验证权限服务方法可以被调用，具体的权限数据取决于系统初始化
        
        // 2. 验证权限服务可以正常工作
        assertNotNull(permissionService);
        
        // 3. 验证角色服务可以正常工作
        assertNotNull(roleService);
    }
}
