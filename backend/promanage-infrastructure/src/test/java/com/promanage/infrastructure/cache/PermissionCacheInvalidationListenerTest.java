package com.promanage.infrastructure.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.promanage.domain.event.RolePermissionChangedEvent;
import com.promanage.domain.event.UserRoleAssignedEvent;
import com.promanage.domain.event.UserRoleRemovedEvent;
import com.promanage.domain.mapper.UserRoleMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 权限缓存失效监听器单元测试
 * 
 * <p>测试权限相关事件触发后的缓存失效逻辑
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("权限缓存失效监听器测试")
class PermissionCacheInvalidationListenerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private Cache userPermissionsCache;

    private PermissionCacheInvalidationListener listener;

    @BeforeEach
    void setUp() {
        listener = new PermissionCacheInvalidationListener(cacheManager, userRoleMapper);
        
        // 默认缓存配置 - 使用lenient避免不必要的stubbing警告
        lenient().when(cacheManager.getCache("userPermissions")).thenReturn(userPermissionsCache);
    }

    @Test
    @DisplayName("角色权限变更事件 - 成功失效相关用户缓存")
    void testHandleRolePermissionChanged_Success() {
        // Given
        Long roleId = 1L;
        List<Long> userIds = Arrays.asList(100L, 101L, 102L);
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);

        // When
        listener.handleRolePermissionChanged(event);

        // Then - 等待异步操作完成
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(userRoleMapper).findUserIdsByRoleId(roleId);
            verify(userPermissionsCache, times(3)).evict(any(Long.class));
            verify(userPermissionsCache).evict(100L);
            verify(userPermissionsCache).evict(101L);
            verify(userPermissionsCache).evict(102L);
        });
    }

    @Test
    @DisplayName("角色权限变更事件 - 无关联用户时跳过缓存失效")
    void testHandleRolePermissionChanged_NoUsers() {
        // Given
        Long roleId = 1L;
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(Collections.emptyList());

        // When
        listener.handleRolePermissionChanged(event);

        // Then
        verify(userRoleMapper).findUserIdsByRoleId(roleId);
        verify(userPermissionsCache, never()).evict(any(Long.class));
    }

    @Test
    @DisplayName("角色权限变更事件 - 数据库查询异常时记录错误但不中断")
    void testHandleRolePermissionChanged_DatabaseException() {
        // Given
        Long roleId = 1L;
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        
        when(userRoleMapper.findUserIdsByRoleId(roleId))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> listener.handleRolePermissionChanged(event));
        
        verify(userRoleMapper).findUserIdsByRoleId(roleId);
        verify(userPermissionsCache, never()).evict(any(Long.class));
    }

    @Test
    @DisplayName("用户角色分配事件 - 成功失效用户缓存")
    void testHandleUserRoleAssigned_Success() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        UserRoleAssignedEvent event = new UserRoleAssignedEvent(userId, roleId);

        // When
        listener.handleUserRoleAssigned(event);

        // Then
        verify(userPermissionsCache).evict(userId);
    }

    @Test
    @DisplayName("用户角色分配事件 - 缓存失效失败时记录错误但不中断")
    void testHandleUserRoleAssigned_CacheEvictionFailed() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        UserRoleAssignedEvent event = new UserRoleAssignedEvent(userId, roleId);
        
        doThrow(new RuntimeException("Cache eviction failed"))
            .when(userPermissionsCache).evict(userId);

        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> listener.handleUserRoleAssigned(event));
        
        verify(userPermissionsCache).evict(userId);
    }

    @Test
    @DisplayName("用户角色移除事件 - 成功失效用户缓存")
    void testHandleUserRoleRemoved_Success() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        UserRoleRemovedEvent event = new UserRoleRemovedEvent(userId, roleId);

        // When
        listener.handleUserRoleRemoved(event);

        // Then
        verify(userPermissionsCache).evict(userId);
    }

    @Test
    @DisplayName("用户角色移除事件 - 缓存失效失败时记录错误但不中断")
    void testHandleUserRoleRemoved_CacheEvictionFailed() {
        // Given
        Long userId = 100L;
        Long roleId = 1L;
        UserRoleRemovedEvent event = new UserRoleRemovedEvent(userId, roleId);
        
        doThrow(new RuntimeException("Cache eviction failed"))
            .when(userPermissionsCache).evict(userId);

        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> listener.handleUserRoleRemoved(event));
        
        verify(userPermissionsCache).evict(userId);
    }

    @Test
    @DisplayName("缓存管理器未配置userPermissions缓存时跳过失效")
    void testHandleRolePermissionChanged_CacheNotConfigured() {
        // Given
        Long roleId = 1L;
        List<Long> userIds = Arrays.asList(100L, 101L);
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        
        when(cacheManager.getCache("userPermissions")).thenReturn(null);
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);

        // When
        listener.handleRolePermissionChanged(event);

        // Then - 等待异步操作完成
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(userRoleMapper).findUserIdsByRoleId(roleId);
            // 当缓存为null时，不会调用evict方法，所以不需要验证userPermissionsCache
        });
    }

    @Test
    @DisplayName("批量用户缓存失效 - 部分成功部分失败")
    void testEvictUserPermissionsCache_PartialSuccess() {
        // Given
        Long roleId = 1L;
        List<Long> userIds = Arrays.asList(100L, 101L, 102L);
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(userIds);
        
        // 模拟部分缓存失效失败
        doNothing().when(userPermissionsCache).evict(100L);
        doThrow(new RuntimeException("Cache error")).when(userPermissionsCache).evict(101L);
        doNothing().when(userPermissionsCache).evict(102L);

        // When
        listener.handleRolePermissionChanged(event);

        // Then - 等待异步操作完成
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(userPermissionsCache).evict(100L);
            verify(userPermissionsCache).evict(101L);
            verify(userPermissionsCache).evict(102L);
        });
    }

    @Test
    @DisplayName("空用户列表时跳过缓存失效")
    void testEvictUserPermissionsCache_EmptyUserList() {
        // Given
        Long roleId = 1L;
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(roleId);
        
        when(userRoleMapper.findUserIdsByRoleId(roleId)).thenReturn(null);

        // When
        listener.handleRolePermissionChanged(event);

        // Then
        verify(userRoleMapper).findUserIdsByRoleId(roleId);
        verify(userPermissionsCache, never()).evict(any(Long.class));
    }
}