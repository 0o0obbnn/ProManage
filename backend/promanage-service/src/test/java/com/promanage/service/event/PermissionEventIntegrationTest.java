package com.promanage.service.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promanage.domain.entity.Role;
import com.promanage.domain.event.RolePermissionChangedEvent;
import com.promanage.domain.event.UserRoleAssignedEvent;
import com.promanage.domain.event.UserRoleRemovedEvent;
import com.promanage.domain.mapper.PermissionMapper;
import com.promanage.domain.mapper.RoleMapper;
import com.promanage.domain.mapper.RolePermissionMapper;
import com.promanage.domain.mapper.UserRoleMapper;
import com.promanage.infrastructure.cache.PermissionCacheInvalidationListener;
import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.impl.RolePermissionServiceImpl;
import com.promanage.service.impl.UserServiceImpl;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.service.IPermissionManagementService;
import com.promanage.service.service.IPermissionService;
import com.promanage.service.service.IUserPermissionService;
import com.promanage.service.strategy.UserAuthStrategy;
import com.promanage.service.strategy.UserProfileStrategy;
import com.promanage.service.strategy.UserQueryStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 权限事件发布集成测试
 * 
 * <p>测试服务层事件发布和监听器的集成工作
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("权限事件发布集成测试")
class PermissionEventIntegrationTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private PermissionCacheInvalidationListener cacheInvalidationListener;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private IUserPermissionService userPermissionService;

    @Mock
    private UserQueryStrategy userQueryStrategy;

    @Mock
    private UserAuthStrategy userAuthStrategy;

    @Mock
    private UserProfileStrategy userProfileStrategy;

    @Mock
    private IPermissionManagementService permissionManagementService;

    @Mock
    private IPermissionService permissionService;

    private UserServiceImpl userService;
    private RolePermissionServiceImpl rolePermissionService;

    @BeforeEach
    void setUp() {
        // 创建服务实例，使用正确的构造函数注入依赖
        userService = new UserServiceImpl(
            userMapper,
            passwordEncoder,
            roleMapper,
            permissionMapper,
            userPermissionService,
            userQueryStrategy,
            userAuthStrategy,
            userProfileStrategy
        );

        rolePermissionService = new RolePermissionServiceImpl(
            roleMapper,
            rolePermissionMapper,
            permissionManagementService,
            permissionService,
            eventPublisher
        );
    }

    @Test
    @DisplayName("用户角色分配 - 发布UserRoleAssignedEvent事件")
    void testUserRoleAssignment_PublishesEvent() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        List<Long> roleIds = Arrays.asList(roleId);
        
        ArgumentCaptor<UserRoleAssignedEvent> eventCaptor = 
            ArgumentCaptor.forClass(UserRoleAssignedEvent.class);

        // When
        userService.assignRoles(userId, roleIds);

        // Then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserRoleAssignedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(roleId, capturedEvent.getRoleId());
    }

    @Test
    @DisplayName("用户角色移除 - 发布UserRoleRemovedEvent事件")
    void testUserRoleRemoval_PublishesEvent() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        
        ArgumentCaptor<UserRoleRemovedEvent> eventCaptor = 
            ArgumentCaptor.forClass(UserRoleRemovedEvent.class);

        // When
        userService.removeRole(userId, roleId);

        // Then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserRoleRemovedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals(roleId, capturedEvent.getRoleId());
    }

    @Test
    @DisplayName("角色权限分配 - 发布RolePermissionChangedEvent事件")
    void testRolePermissionAssignment_PublishesEvent() {
        // Given
        Long organizationId = 1L;
        Long roleId = 1L;
        List<Long> permissionIds = Arrays.asList(10L, 11L, 12L);
        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(permissionIds);
        
        ArgumentCaptor<RolePermissionChangedEvent> eventCaptor = 
            ArgumentCaptor.forClass(RolePermissionChangedEvent.class);

        // Mock role exists
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(false);
        when(roleMapper.selectById(roleId)).thenReturn(role);
        
        // When
        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);

        // Then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        RolePermissionChangedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(roleId, capturedEvent.getRoleId());
    }

    @Test
    @DisplayName("角色权限移除 - 发布RolePermissionChangedEvent事件")
    void testRolePermissionRemoval_PublishesEvent() {
        // Given
        Long organizationId = 1L;
        Long roleId = 1L;
        // 空列表表示移除所有权限
        List<Long> permissionIds = Arrays.asList();
        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(permissionIds);

        // Mock role exists
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(false);
        when(roleMapper.selectById(roleId)).thenReturn(role);
        
        // When
        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);

        // Then - 当权限列表为空时，应该不发布事件（因为会删除所有权限但不发布事件）
        verify(eventPublisher, never()).publishEvent(any(RolePermissionChangedEvent.class));
    }

    @Test
    @DisplayName("事件发布 - 验证事件发布时机")
    void testEventPublishing_Timing() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        List<Long> roleIds = Arrays.asList(roleId);
        
        CountDownLatch latch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(eventPublisher).publishEvent(any(UserRoleAssignedEvent.class));

        // When
        userService.assignRoles(userId, roleIds);

        // Then
        try {
            assertTrue(latch.await(1, TimeUnit.SECONDS), "事件应该在1秒内发布");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("等待事件发布被中断");
        }
    }

    @Test
    @DisplayName("批量角色分配 - 发布多个事件")
    void testBatchRoleAssignment_PublishesMultipleEvents() {
        // Given
        Long userId = 100L;
        List<Long> roleIds = Arrays.asList(1L, 2L, 3L);
        
        ArgumentCaptor<UserRoleAssignedEvent> eventCaptor = 
            ArgumentCaptor.forClass(UserRoleAssignedEvent.class);

        // When
        userService.assignRoles(userId, roleIds);

        // Then
        verify(eventPublisher, times(3)).publishEvent(eventCaptor.capture());
        
        List<UserRoleAssignedEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(3, capturedEvents.size());
        
        // 验证每个事件都包含正确的用户ID和角色ID
        for (int i = 0; i < 3; i++) {
            assertEquals(userId, capturedEvents.get(i).getUserId());
            assertEquals(roleIds.get(i), capturedEvents.get(i).getRoleId());
        }
    }

    @Test
    @DisplayName("事件发布异常 - 不影响主业务流程")
    void testEventPublishingException_DoesNotAffectMainFlow() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        List<Long> roleIds = Arrays.asList(roleId);
        
        doThrow(new RuntimeException("Event publishing failed"))
            .when(eventPublisher).publishEvent(any(UserRoleAssignedEvent.class));

        // When & Then - 主业务流程不应该因为事件发布失败而中断
        assertDoesNotThrow(() -> userService.assignRoles(userId, roleIds));
        
        verify(eventPublisher).publishEvent(any(UserRoleAssignedEvent.class));
    }

    @Test
    @DisplayName("空角色列表 - 不发布事件")
    void testEmptyRoleList_DoesNotPublishEvent() {
        // Given
        Long userId = 100L;
        List<Long> roleIds = Arrays.asList();

        // When
        userService.assignRoles(userId, roleIds);

        // Then
        verify(eventPublisher, never()).publishEvent(any(UserRoleAssignedEvent.class));
    }

    @Test
    @DisplayName("空权限列表 - 不发布事件")
    void testEmptyPermissionList_DoesNotPublishEvent() {
        // Given
        Long organizationId = 1L;
        Long roleId = 1L;
        List<Long> permissionIds = Arrays.asList();
        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(permissionIds);

        // Mock role exists
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(false);
        when(roleMapper.selectById(roleId)).thenReturn(role);

        // When
        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);

        // Then
        verify(eventPublisher, never()).publishEvent(any(RolePermissionChangedEvent.class));
    }
}
