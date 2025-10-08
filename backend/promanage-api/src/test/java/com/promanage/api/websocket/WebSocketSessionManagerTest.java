package com.promanage.api.websocket;

import com.promanage.api.TestConfig;
import com.promanage.common.websocket.WebSocketMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WebSocket会话管理器测试
 */
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class WebSocketSessionManagerTest {

    private WebSocketSessionManager sessionManager;
    
    @Mock
    private WebSocketSessionHandler sessionHandler;

    @BeforeEach
    void setUp() {
        sessionManager = new WebSocketSessionManager();
    }

    @Test
    void testAddUserSession() {
        // 添加用户会话
        sessionManager.addUserSession(1L, "session1", sessionHandler);
        
        // 验证会话已添加
        assertTrue(sessionManager.isUserOnline(1L));
        assertEquals(1, sessionManager.getOnlineUserCount());
    }

    @Test
    void testRemoveUserSession() {
        // 添加用户会话
        sessionManager.addUserSession(1L, "session1", sessionHandler);
        
        // 移除用户会话
        sessionManager.removeUserSession("session1");
        
        // 验证会话已移除
        assertFalse(sessionManager.isUserOnline(1L));
        assertEquals(0, sessionManager.getOnlineUserCount());
    }

    @Test
    void testSendMessageToUser() {
        // 模拟sessionHandler返回true
        when(sessionHandler.isActive()).thenReturn(true);
        when(sessionHandler.sendMessage(any(WebSocketMessage.class))).thenReturn(true);
        
        // 添加用户会话
        sessionManager.addUserSession(1L, "session1", sessionHandler);
        
        // 发送消息
        WebSocketMessage message = WebSocketMessage.notification("测试标题", "测试内容", null, null);
        boolean result = sessionManager.sendMessageToUser(1L, message);
        
        // 验证消息发送成功
        assertTrue(result);
        verify(sessionHandler, times(1)).sendMessage(message);
    }

    @Test
    void testSendMessageToUserOffline() {
        // 不添加用户会话（用户离线）
        
        // 发送消息
        WebSocketMessage message = WebSocketMessage.notification("测试标题", "测试内容", null, null);
        boolean result = sessionManager.sendMessageToUser(1L, message);
        
        // 验证消息发送失败
        assertFalse(result);
    }

    @Test
    void testBroadcastMessage() {
        // 模拟sessionHandler返回true
        when(sessionHandler.isActive()).thenReturn(true);
        when(sessionHandler.sendMessage(any(WebSocketMessage.class))).thenReturn(true);
        
        // 添加多个用户会话
        sessionManager.addUserSession(1L, "session1", sessionHandler);
        sessionManager.addUserSession(2L, "session2", sessionHandler);
        
        // 广播消息
        WebSocketMessage message = WebSocketMessage.system("系统消息", "测试内容");
        sessionManager.broadcastMessage(message);
        
        // 验证每个在线用户都收到消息
        verify(sessionHandler, times(2)).sendMessage(message);
    }

    @Test
    void testUpdateHeartbeat() {
        // 添加用户会话
        sessionManager.addUserSession(1L, "session1", sessionHandler);
        
        // 获取初始心跳时间
        WebSocketSessionManager.WebSocketSessionInfo sessionInfo = sessionManager.getUserSessionInfo(1L);
        long initialHeartbeat = sessionInfo.getLastHeartbeat();
        
        // 等待一小段时间后更新心跳
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        sessionManager.updateHeartbeat("session1");
        
        // 验证心跳时间已更新
        WebSocketSessionManager.WebSocketSessionInfo updatedSessionInfo = sessionManager.getUserSessionInfo(1L);
        assertTrue(updatedSessionInfo.getLastHeartbeat() > initialHeartbeat);
    }

    @Test
    void testGetOnlineUserIds() {
        // 添加多个用户会话
        sessionManager.addUserSession(1L, "session1", sessionHandler);
        sessionManager.addUserSession(2L, "session2", sessionHandler);
        
        // 获取在线用户ID列表
        var onlineUserIds = sessionManager.getOnlineUserIds();
        
        // 验证在线用户ID
        assertEquals(2, onlineUserIds.size());
        assertTrue(onlineUserIds.contains(1L));
        assertTrue(onlineUserIds.contains(2L));
    }
}