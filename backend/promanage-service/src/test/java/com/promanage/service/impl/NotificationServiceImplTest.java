package com.promanage.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.entity.Notification;
import com.promanage.service.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** 通知服务测试类 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(100L);
        testNotification.setType("TASK_CREATED");
        testNotification.setTitle("测试通知");
        testNotification.setContent("这是一个测试通知");
        testNotification.setIsRead(false);
        testNotification.setCreateTime(LocalDateTime.now());
        testNotification.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testSendNotification_Success() {
        // Given
        Long userId = 100L;
        String type = "TASK_CREATED";
        String title = "测试通知";
        String content = "这是一个测试通知";

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendNotification(
            userId,
            type,
            title,
            content
        );

        // Then
        assertTrue(result);
        verify(notificationMapper).insert(any(Notification.class));
    }

    @Test
    void testSendNotificationWithRelatedData_Success() {
        // Given
        Long userId = 100L;
        String type = "TASK_CREATED";
        String title = "测试通知";
        String content = "这是一个测试通知";
        Long relatedId = 1L;
        String relatedType = "TASK";
        Long creatorId = 1L;

        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);

        // When
        boolean result = notificationService.sendNotification(
            userId,
            type,
            title,
            content,
            relatedId,
            relatedType,
            creatorId
        );

        // Then
        assertTrue(result);
        verify(notificationMapper).insert(any(Notification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetUserNotifications_Success() {
        // Given
        Long userId = 100L;
        int page = 1;
        int size = 10;
        Page<Notification> expectedPage = new Page<>(page, size);
        expectedPage.setRecords(Arrays.asList(testNotification));

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(
                notificationMapper.selectPage(
                    any(Page.class),
                    org.mockito.ArgumentMatchers.<
                            com.baomidou.mybatisplus.core.conditions.Wrapper<
                                Notification
                            >
                        >any()
                )
            ).thenReturn(expectedPage);

            // When
            Page<Notification> result =
                notificationService.getUserNotifications(userId, page, size);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(notificationMapper).selectPage(
                any(Page.class),
                org.mockito.ArgumentMatchers.<
                        com.baomidou.mybatisplus.core.conditions.Wrapper<
                            Notification
                        >
                    >any()
            );
        }
    }

    @Test
    void testGetUnreadCount_Success() {
        // Given
        Long userId = 100L;
        int expectedCount = 5;

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(notificationMapper.countUnreadByUserId(userId)).thenReturn(
                expectedCount
            );

            // When
            int result = notificationService.getUnreadCount(userId);

            // Then
            assertEquals(expectedCount, result);
            verify(notificationMapper).countUnreadByUserId(userId);
        }
    }

    @Test
    void testMarkAsRead_Success() {
        // Given
        Long notificationId = 1L;
        Long userId = 100L;
        when(notificationMapper.selectById(notificationId)).thenReturn(
            testNotification
        );

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(
                notificationMapper.markAsRead(notificationId, userId)
            ).thenReturn(1);

            // When
            boolean result = notificationService.markAsRead(
                notificationId,
                userId
            );

            // Then
            assertTrue(result);
            verify(notificationMapper).markAsRead(notificationId, userId);
        }
    }

    @Test
    void testMarkAsReadBatch_Success() {
        // Given
        List<Long> notificationIds = Arrays.asList(1L, 2L);
        Long userId = 100L;

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(
                notificationMapper.markAsReadBatch(notificationIds, userId)
            ).thenReturn(2);

            // When
            boolean result = notificationService.markAsReadBatch(
                notificationIds,
                userId
            );

            // Then
            assertTrue(result);
            verify(notificationMapper).markAsReadBatch(notificationIds, userId);
        }
    }

    @Test
    void testMarkAllAsRead_Success() {
        // Given
        Long userId = 100L;

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(notificationMapper.markAllAsRead(userId)).thenReturn(5);

            // When
            boolean result = notificationService.markAllAsRead(userId);

            // Then
            assertTrue(result);
            verify(notificationMapper).markAllAsRead(userId);
        }
    }

    @Test
    void testDeleteNotification_Success() {
        // Given
        Long notificationId = 1L;
        Long userId = 100L;
        when(notificationMapper.selectById(notificationId)).thenReturn(
            testNotification
        );

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(
                notificationMapper.deleteNotification(notificationId, userId)
            ).thenReturn(1);

            // When
            boolean result = notificationService.deleteNotification(
                notificationId,
                userId
            );

            // Then
            assertTrue(result);
            verify(notificationMapper).deleteNotification(
                notificationId,
                userId
            );
        }
    }

    @Test
    void testDeleteNotificationBatch_Success() {
        // Given
        List<Long> notificationIds = Arrays.asList(1L, 2L);
        Long userId = 100L;

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(
                notificationMapper.deleteNotificationBatch(
                    notificationIds,
                    userId
                )
            ).thenReturn(2);

            // When
            boolean result = notificationService.deleteNotificationBatch(
                notificationIds,
                userId
            );

            // Then
            assertTrue(result);
            verify(notificationMapper).deleteNotificationBatch(
                notificationIds,
                userId
            );
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetNotificationsByType_Success() {
        // Given
        Long userId = 100L;
        String type = "TASK_CREATED";
        int page = 1;
        int size = 10;
        Page<Notification> expectedPage = new Page<>(page, size);
        expectedPage.setRecords(Arrays.asList(testNotification));

        try (
            MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(
                SecurityUtils.class
            )
        ) {
            mockedSecurityUtils
                .when(SecurityUtils::getCurrentUserId)
                .thenReturn(Optional.of(userId));
            when(
                notificationMapper.selectPage(
                    any(Page.class),
                    org.mockito.ArgumentMatchers.<
                            com.baomidou.mybatisplus.core.conditions.Wrapper<
                                Notification
                            >
                        >any()
                )
            ).thenReturn(expectedPage);

            // When
            Page<Notification> result =
                notificationService.getNotificationsByType(
                    userId,
                    type,
                    page,
                    size
                );

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            verify(notificationMapper).selectPage(any(Page.class), any());
        }
    }

    @Test
    void testGetNotificationsByRelatedData_Success() {
        // Given
        Long relatedId = 1L;
        String relatedType = "TASK";
        List<Notification> expectedNotifications = Arrays.asList(
            testNotification
        );

        when(
            notificationMapper.findByRelatedData(relatedId, relatedType)
        ).thenReturn(expectedNotifications);

        // When
        List<Notification> result =
            notificationService.getNotificationsByRelatedData(
                relatedId,
                relatedType
            );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationMapper).findByRelatedData(relatedId, relatedType);
    }
}
