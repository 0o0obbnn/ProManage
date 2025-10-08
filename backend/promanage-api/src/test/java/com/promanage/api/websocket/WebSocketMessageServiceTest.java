package com.promanage.api.websocket;

import com.promanage.common.websocket.WebSocketMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WebSocket消息服务测试
 */
@ExtendWith(MockitoExtension.class)
class WebSocketMessageServiceTest {

    @Mock
    private WebSocketSessionManager sessionManager;

    @InjectMocks
    private WebSocketMessageService webSocketMessageService;

    @Test
    void testSendNotificationToUser() {
        // 模拟返回true
        when(sessionManager.sendMessageToUser(any(Long.class), any(WebSocketMessage.class)))
                .thenReturn(true);

        // 发送通知
        boolean result = webSocketMessageService.sendNotificationToUser(1L, "测试标题", "测试内容");

        // 验证返回true
        assertTrue(result);
        verify(sessionManager, times(1)).sendMessageToUser(eq(1L), any(WebSocketMessage.class));
    }

    @Test
    void testSendNotificationToUserWithRelatedData() {
        // 模拟返回true
        when(sessionManager.sendMessageToUser(any(Long.class), any(WebSocketMessage.class)))
                .thenReturn(true);

        // 发送带关联数据的通知
        boolean result = webSocketMessageService.sendNotificationToUser(
                1L, "测试标题", "测试内容", 100L, "task");

        // 验证返回true
        assertTrue(result);
        verify(sessionManager, times(1)).sendMessageToUser(eq(1L), any(WebSocketMessage.class));
    }

    @Test
    void testSendNotificationToUsers() {
        // 模拟返回true
        when(sessionManager.sendMessageToUser(any(Long.class), any(WebSocketMessage.class)))
                .thenReturn(true);

        List<Long> userIds = Arrays.asList(1L, 2L, 3L);

        // 批量发送通知
        webSocketMessageService.sendNotificationToUsers(userIds, "测试标题", "测试内容");

        // 验证sessionManager的sendMessageToUsers方法被调用
        verify(sessionManager, times(1)).sendMessageToUsers(eq(userIds), any(WebSocketMessage.class));
    }

    @Test
    void testBroadcastNotification() {
        // 广播系统消息
        webSocketMessageService.broadcastNotification("系统标题", "系统内容");

        // 验证调用了sessionManager的broadcastMessage方法
        verify(sessionManager, times(1)).broadcastMessage(any(WebSocketMessage.class));
    }

    @Test
    void testSendProjectNotification() {
        // 发送项目通知
        webSocketMessageService.sendProjectNotification(100L, "项目标题", "项目内容");

        // 验证调用了sessionManager的broadcastMessage方法
        verify(sessionManager, times(1)).broadcastMessage(any(WebSocketMessage.class));
    }

    // 以下方法在当前接口中不存在，暂时注释掉
    /*
    @Test
    void testSendTaskNotification() {
        // 模拟返回true
        when(notificationWebSocketHandler.sendMessageToUser(any(Long.class), any(WebSocketMessage.class)))
                .thenReturn(true);

        // 发送任务通知
        boolean result = webSocketMessageService.sendTaskNotification(1L, "任务标题", "任务内容", 100L);

        // 验证返回true
        assertTrue(result);
        verify(notificationWebSocketHandler, times(1)).sendMessageToUser(eq(1L), any(WebSocketMessage.class));
    }

    @Test
    void testSendDocumentNotification() {
        // 模拟返回true
        when(notificationWebSocketHandler.sendMessageToUser(any(Long.class), any(WebSocketMessage.class)))
                .thenReturn(true);

        // 发送文档通知
        boolean result = webSocketMessageService.sendDocumentNotification(1L, "文档标题", "文档内容", 100L);

        // 验证返回true
        assertTrue(result);
        verify(notificationWebSocketHandler, times(1)).sendMessageToUser(eq(1L), any(WebSocketMessage.class));
    }

    @Test
    void testSendChangeRequestNotification() {
        // 模拟返回true
        when(notificationWebSocketHandler.sendMessageToUser(any(Long.class), any(WebSocketMessage.class)))
                .thenReturn(true);

        // 发送变更请求通知
        boolean result = webSocketMessageService.sendChangeRequestNotification(
                1L, "变更标题", "变更内容", 100L);

        // 验证返回true
        assertTrue(result);
        verify(notificationWebSocketHandler, times(1)).sendMessageToUser(eq(1L), any(WebSocketMessage.class));
    }

    @Test
    void testIsUserOnline() {
        // 模拟用户在线
        when(notificationWebSocketHandler.isUserOnline(1L)).thenReturn(true);
        when(notificationWebSocketHandler.isUserOnline(2L)).thenReturn(false);

        // 检查用户在线状态
        assertTrue(webSocketMessageService.isUserOnline(1L));
        assertFalse(webSocketMessageService.isUserOnline(2L));
    }

    @Test
    void testGetOnlineUserCount() {
        // 模拟在线用户数量
        when(notificationWebSocketHandler.getOnlineUserCount()).thenReturn(5);

        // 获取在线用户数量
        int count = webSocketMessageService.getOnlineUserCount();

        // 验证数量
        assertEquals(5, count);
    }
    */
}