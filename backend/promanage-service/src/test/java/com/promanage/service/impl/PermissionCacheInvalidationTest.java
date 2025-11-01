package com.promanage.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.promanage.common.entity.User;
import com.promanage.domain.event.UserRoleAssignedEvent;
import com.promanage.domain.event.UserRoleRemovedEvent;
import com.promanage.domain.mapper.UserMapper;
import com.promanage.domain.mapper.UserRoleMapper;
import com.promanage.infrastructure.utils.SecurityUtils;

/**
 * 权限缓存失效功能测试
 * 测试UserServiceImpl中的事件发布功能
 *
 * @author ProManage Team
 * @date 2025-01-06
 */
@ExtendWith(MockitoExtension.class)
class PermissionCacheInvalidationTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // 设置基本的Mock行为
        lenient().when(userMapper.selectById(anyLong())).thenReturn(createTestUser());
        lenient().when(userRoleMapper.selectByUserId(anyLong())).thenReturn(Arrays.asList());
        lenient().when(userRoleMapper.existsByUserIdAndRoleId(anyLong(), anyLong())).thenReturn(false);
        lenient().when(userRoleMapper.batchInsert(any())).thenReturn(1);
        lenient().when(userRoleMapper.insert(any())).thenReturn(1);
        lenient().when(userRoleMapper.deleteByUserIdAndRoleId(anyLong(), anyLong())).thenReturn(1);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setDeleted(false);
        return user;
    }

    @Test
    void testAssignRoles_PublishesUserRoleAssignedEvents() {
        // Given
        Long userId = 1L;
        List<Long> roleIds = Arrays.asList(2L, 3L);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(java.util.Optional.of(1L));
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(java.util.Optional.of("testuser"));

            // When
            userService.assignRoles(userId, roleIds);

            // Then
            verify(eventPublisher, times(2)).publishEvent(any(UserRoleAssignedEvent.class));
        }
    }

    @Test
    void testAddRole_PublishesUserRoleAssignedEvent() {
        // Given
        Long userId = 1L;
        Long roleId = 2L;

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(java.util.Optional.of(1L));
            mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(java.util.Optional.of("testuser"));

            // When
            userService.addRole(userId, roleId);

            // Then
            verify(eventPublisher, times(1)).publishEvent(any(UserRoleAssignedEvent.class));
        }
    }

    @Test
    void testRemoveRole_PublishesUserRoleRemovedEvent() {
        // Given
        Long userId = 1L;
        Long roleId = 2L;

        when(userRoleMapper.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);

        // When
        userService.removeRole(userId, roleId);

        // Then
        verify(eventPublisher, times(1)).publishEvent(any(UserRoleRemovedEvent.class));
    }
}
