package com.promanage.service.integration;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
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
 * 权限变更端到端测试
 * 
 * <p>测试完整的权限变更流程：角色权限变更 -> 事件发布 -> 缓存失效 -> 权限查询
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("权限变更端到端测试")
class PermissionChangeEndToEndTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache userPermissionsCache;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    private PermissionCacheInvalidationListener cacheListener;

    private Role testRole;
    private Permission testPermission1;
    private Permission testPermission2;
    private Permission testPermission3;

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

        cacheListener = new PermissionCacheInvalidationListener(cacheManager, userRoleMapper);

        // 初始化测试数据
        setupTestData();

        // 配置默认Mock行为
        when(cacheManager.getCache("userPermissions")).thenReturn(userPermissionsCache);
    }

    private void setupTestData() {
        // 创建测试角色
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("项目经理");
        testRole.setRoleCode("ROLE_PM");
        testRole.setStatus(0);

        // 创建测试权限
        testPermission1 = new Permission();
        testPermission1.setId(10L);
        testPermission1.setPermissionName("创建文档");
        testPermission1.setPermissionCode("document:create");
        testPermission1.setType("api");

        testPermission2 = new Permission();
        testPermission2.setId(11L);
        testPermission2.setPermissionName("编辑文档");
        testPermission2.setPermissionCode("document:edit");
        testPermission2.setType("api");

        testPermission3 = new Permission();
        testPermission3.setId(12L);
        testPermission3.setPermissionName("删除文档");
        testPermission3.setPermissionCode("document:delete");
        testPermission3.setType("api");
    }

    @Test
    @DisplayName("完整权限变更流程 - 角色权限分配")
    void testCompletePermissionChangeFlow_RolePermissionAssignment() {
        // Given
        Long roleId = testRole.getId();
        List<Long> permissionIds = Arrays.asList(testPermission1.getId(), testPermission2.getId());
        List<Long> userIds = Arrays.asList(100L, 101L, 102L);

        // 配置Mock行为
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);
        when(rolePermissionMapper.selectPermissionIdsByRoleId(roleId))
            .thenReturn(Arrays.asList(10L, 11L)); // 模拟现有权限

        // Mock role exists
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(false);
        when(roleMapper.selectById(roleId)).thenReturn(role);

        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(permissionIds);

        // 使用CountDownLatch等待事件处理完成
        CountDownLatch eventLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            // 模拟事件发布后立即处理缓存失效
            cacheListener.handleRolePermissionChanged(invocation.getArgument(0));
            eventLatch.countDown();
            return null;
        }).when(eventPublisher).publishEvent(any());

        // When
        Long organizationId = 1L;
        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);

        // Then
        try {
            // 等待事件处理完成
            assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "事件处理未在5秒内完成");

            // 验证事件发布
            verify(eventPublisher).publishEvent(any());

            // 验证缓存失效
            verify(userPermissionsCache, times(userIds.size())).evict(any(Long.class));
            verify(userPermissionsCache).evict(100L);
            verify(userPermissionsCache).evict(101L);
            verify(userPermissionsCache).evict(102L);

            // 验证数据库查询
            verify(userRoleMapper).findUserIdsByRoleId(roleId);

            log.info("权限变更端到端测试完成：角色{}分配权限{}，影响用户{}", 
                roleId, permissionIds, userIds);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        }
    }

    @Test
    @DisplayName("完整权限变更流程 - 用户角色分配")
    void testCompletePermissionChangeFlow_UserRoleAssignment() {
        // Given
        Long userId = 100L;
        Long roleId = testRole.getId();
        List<Long> roleIds = Arrays.asList(roleId);

        // 使用CountDownLatch等待事件处理完成
        CountDownLatch eventLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            // 模拟事件发布后立即处理缓存失效
            cacheListener.handleUserRoleAssigned(invocation.getArgument(0));
            eventLatch.countDown();
            return null;
        }).when(eventPublisher).publishEvent(any());

        // When
        userService.assignRoles(userId, roleIds);

        // Then
        try {
            // 等待事件处理完成
            assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "事件处理未在5秒内完成");

            // 验证事件发布
            verify(eventPublisher).publishEvent(any());

            // 验证缓存失效
            verify(userPermissionsCache).evict(userId);

            log.info("用户角色分配端到端测试完成：用户{}分配角色{}", userId, roleIds);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        }
    }

    @Test
    @DisplayName("完整权限变更流程 - 用户角色移除")
    void testCompletePermissionChangeFlow_UserRoleRemoval() {
        // Given
        Long userId = 100L;
        Long roleId = testRole.getId();

        // 使用CountDownLatch等待事件处理完成
        CountDownLatch eventLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            // 模拟事件发布后立即处理缓存失效
            cacheListener.handleUserRoleRemoved(invocation.getArgument(0));
            eventLatch.countDown();
            return null;
        }).when(eventPublisher).publishEvent(any());

        // When
        userService.removeRole(userId, roleId);

        // Then
        try {
            // 等待事件处理完成
            assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "事件处理未在5秒内完成");

            // 验证事件发布
            verify(eventPublisher).publishEvent(any());

            // 验证缓存失效
            verify(userPermissionsCache).evict(userId);

            log.info("用户角色移除端到端测试完成：用户{}移除角色{}", userId, roleId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        }
    }

    @Test
    @DisplayName("权限变更流程 - 异常情况处理")
    void testPermissionChangeFlow_ExceptionHandling() {
        // Given
        Long roleId = testRole.getId();
        List<Long> permissionIds = Arrays.asList(testPermission1.getId());

        // 配置Mock行为 - 模拟数据库查询异常
        when(userRoleMapper.findUserIdsByRoleId(roleId))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Mock role exists
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(false);
        when(roleMapper.selectById(roleId)).thenReturn(role);

        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(permissionIds);

        // 使用CountDownLatch等待事件处理完成
        CountDownLatch eventLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            try {
                // 模拟事件发布后立即处理缓存失效
                cacheListener.handleRolePermissionChanged(invocation.getArgument(0));
            } catch (Exception e) {
                log.warn("缓存失效处理异常", e);
            } finally {
                eventLatch.countDown();
            }
            return null;
        }).when(eventPublisher).publishEvent(any());

        // When
        Long organizationId = 1L;
        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);

        // Then
        try {
            // 等待事件处理完成
            assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "事件处理未在5秒内完成");

            // 验证事件仍然发布（主业务流程不受影响）
            verify(eventPublisher).publishEvent(any());

            // 验证缓存失效被跳过（因为数据库查询失败）
            verify(userPermissionsCache, never()).evict(any(Long.class));

            log.info("异常情况处理测试完成：数据库异常时主业务流程正常，缓存失效被跳过");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        }
    }

    @Test
    @DisplayName("权限变更流程 - 缓存失效失败处理")
    void testPermissionChangeFlow_CacheEvictionFailure() {
        // Given
        Long roleId = testRole.getId();
        List<Long> permissionIds = Arrays.asList(testPermission1.getId());
        List<Long> userIds = Arrays.asList(100L, 101L);

        // 配置Mock行为
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);
        
        // Mock role exists
        Role role = new Role();
        role.setId(roleId);
        role.setDeleted(false);
        when(roleMapper.selectById(roleId)).thenReturn(role);

        AssignPermissionsRequest request = new AssignPermissionsRequest();
        request.setPermissionIds(permissionIds);

        // 模拟缓存失效失败
        doThrow(new RuntimeException("Cache eviction failed"))
            .when(userPermissionsCache).evict(any(Long.class));

        // 使用CountDownLatch等待事件处理完成
        CountDownLatch eventLatch = new CountDownLatch(1);
        doAnswer(invocation -> {
            try {
                // 模拟事件发布后立即处理缓存失效
                cacheListener.handleRolePermissionChanged(invocation.getArgument(0));
            } catch (Exception e) {
                log.warn("缓存失效处理异常", e);
            } finally {
                eventLatch.countDown();
            }
            return null;
        }).when(eventPublisher).publishEvent(any());

        // When
        Long organizationId = 1L;
        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);

        // Then
        try {
            // 等待事件处理完成
            assertTrue(eventLatch.await(5, TimeUnit.SECONDS), "事件处理未在5秒内完成");

            // 验证事件仍然发布（主业务流程不受影响）
            verify(eventPublisher).publishEvent(any());

            // 验证尝试了缓存失效（即使失败）
            verify(userPermissionsCache, times(userIds.size())).evict(any(Long.class));

            log.info("缓存失效失败处理测试完成：缓存失效失败时主业务流程正常");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("测试被中断");
        }
    }

    @Test
    @DisplayName("权限变更流程 - 并发场景测试")
    void testPermissionChangeFlow_ConcurrentScenario() throws InterruptedException {
        // Given
        int threadCount = 5;
        int eventsPerThread = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        // 配置Mock行为
        when(userRoleMapper.findUserIdsByRoleId(anyLong()))
            .thenReturn(Arrays.asList(100L, 101L));

        // 使用CountDownLatch等待事件处理完成
        CountDownLatch eventLatch = new CountDownLatch(threadCount * eventsPerThread);
        doAnswer(invocation -> {
            try {
                // 模拟事件发布后立即处理缓存失效
                cacheListener.handleRolePermissionChanged(invocation.getArgument(0));
            } catch (Exception e) {
                log.warn("并发场景下缓存失效处理异常", e);
            } finally {
                eventLatch.countDown();
            }
            return null;
        }).when(eventPublisher).publishEvent(any());

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待所有线程同时开始
                    
                    for (int j = 0; j < eventsPerThread; j++) {
                        Long roleId = (long) (threadIndex * eventsPerThread + j);
                        List<Long> permissionIds = Arrays.asList(testPermission1.getId());
                        
                        // Mock role exists for each roleId
                        Role role = new Role();
                        role.setId(roleId);
                        role.setDeleted(false);
                        when(roleMapper.selectById(roleId)).thenReturn(role);

                        AssignPermissionsRequest request = new AssignPermissionsRequest();
                        request.setPermissionIds(permissionIds);

                        Long organizationId = 1L;
                        rolePermissionService.assignPermissionsToRole(organizationId, roleId, request);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // 开始执行
        boolean finished = finishLatch.await(30, TimeUnit.SECONDS);
        boolean eventsProcessed = eventLatch.await(30, TimeUnit.SECONDS);

        // Then
        assertTrue(finished, "并发测试未在30秒内完成");
        assertTrue(eventsProcessed, "事件处理未在30秒内完成");

        // 验证事件发布次数
        verify(eventPublisher, times(threadCount * eventsPerThread)).publishEvent(any());

        // 验证缓存失效次数（每个事件影响2个用户）
        verify(userPermissionsCache, times(threadCount * eventsPerThread * 2)).evict(any(Long.class));

        log.info("并发场景测试完成：{}个线程，每个线程{}个事件", threadCount, eventsPerThread);
    }
}
