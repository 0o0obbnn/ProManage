package com.promanage.api.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promanage.common.websocket.WebSocketMessage;

/** WebSocket核心功能简单测试 */
@ExtendWith(MockitoExtension.class)
class WebSocketCoreTest {

  @Mock private WebSocketSessionHandler sessionHandler;

  private WebSocketSessionManager sessionManager;

  @BeforeEach
  void setUp() {
    sessionManager = new WebSocketSessionManager();
  }

  @Test
  void testWebSocketMessageBuilder() {
    WebSocketMessage message =
        WebSocketMessage.builder().type("notification").title("测试标题").content("测试内容").build();

    assertNotNull(message);
    assertEquals("notification", message.getType());
    assertEquals("测试标题", message.getTitle());
    assertEquals("测试内容", message.getContent());
    assertNotNull(message.getTimestamp());
  }

  @Test
  void testWebSocketMessageStaticMethods() {
    WebSocketMessage systemMsg = WebSocketMessage.system("系统消息", "系统内容");
    assertEquals("system", systemMsg.getType());
    assertEquals("系统消息", systemMsg.getTitle());
    assertEquals("系统内容", systemMsg.getContent());

    WebSocketMessage heartbeatMsg = WebSocketMessage.heartbeat();
    assertEquals("heartbeat", heartbeatMsg.getType());
    assertEquals("pong", heartbeatMsg.getContent());

    WebSocketMessage errorMsg = WebSocketMessage.error("错误信息");
    assertEquals("error", errorMsg.getType());
    assertEquals("错误信息", errorMsg.getContent());

    WebSocketMessage notificationMsg = WebSocketMessage.notification("通知标题", "通知内容", 1L, "task");
    assertEquals("notification", notificationMsg.getType());
    assertEquals("通知标题", notificationMsg.getTitle());
    assertEquals("通知内容", notificationMsg.getContent());
    assertEquals(1L, notificationMsg.getRelatedId());
    assertEquals("task", notificationMsg.getRelatedType());
  }

  @Test
  void testSessionManagerAddUser() {
    when(sessionHandler.isActive()).thenReturn(true);

    sessionManager.addUserSession(1L, "session1", sessionHandler);

    assertTrue(sessionManager.isUserOnline(1L));
    assertEquals(1, sessionManager.getOnlineUserCount());
    assertTrue(sessionManager.getOnlineUserIds().contains(1L));

    WebSocketSessionManager.WebSocketSessionInfo sessionInfo =
        sessionManager.getUserSessionInfo(1L);
    assertNotNull(sessionInfo);
    assertEquals(1L, sessionInfo.getUserId());
    assertEquals("session1", sessionInfo.getSessionId());
    assertEquals(sessionHandler, sessionInfo.getHandler());
  }

  @Test
  void testSessionManagerRemoveUser() {
    when(sessionHandler.isActive()).thenReturn(true);
    sessionManager.addUserSession(1L, "session1", sessionHandler);

    assertTrue(sessionManager.isUserOnline(1L));

    sessionManager.removeUserSession("session1");

    assertFalse(sessionManager.isUserOnline(1L));
    assertEquals(0, sessionManager.getOnlineUserCount());
  }

  @Test
  void testSessionManagerSendMessage() {
    when(sessionHandler.isActive()).thenReturn(true);
    when(sessionHandler.sendMessage(any(WebSocketMessage.class))).thenReturn(true);

    sessionManager.addUserSession(1L, "session1", sessionHandler);

    WebSocketMessage message = WebSocketMessage.system("测试消息", "测试内容");
    boolean result = sessionManager.sendMessageToUser(1L, message);

    assertTrue(result);
    verify(sessionHandler, times(1)).sendMessage(message);
  }

  @Test
  void testSessionManagerSendToOfflineUser() {
    when(sessionHandler.isActive()).thenReturn(false);

    sessionManager.addUserSession(1L, "session1", sessionHandler);

    WebSocketMessage message = WebSocketMessage.system("测试消息", "测试内容");
    boolean result = sessionManager.sendMessageToUser(1L, message);

    assertFalse(result);
    verify(sessionHandler, never()).sendMessage(any());
  }

  @Test
  void testSessionManagerBroadcast() {
    when(sessionHandler.isActive()).thenReturn(true);
    when(sessionHandler.sendMessage(any(WebSocketMessage.class))).thenReturn(true);

    sessionManager.addUserSession(1L, "session1", sessionHandler);
    sessionManager.addUserSession(2L, "session2", sessionHandler);

    WebSocketMessage message = WebSocketMessage.system("广播消息", "广播内容");
    sessionManager.broadcastMessage(message);

    verify(sessionHandler, times(2)).sendMessage(message);
  }

  @Test
  void testSessionManagerUpdateHeartbeat() {
    lenient().when(sessionHandler.isActive()).thenReturn(true);
    sessionManager.addUserSession(1L, "session1", sessionHandler);

    WebSocketSessionManager.WebSocketSessionInfo sessionInfo =
        sessionManager.getUserSessionInfo(1L);
    long originalHeartbeat = sessionInfo.getLastHeartbeat();

    // 等待一小段时间
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    sessionManager.updateHeartbeat("session1");
    sessionInfo = sessionManager.getUserSessionInfo(1L);

    assertTrue(sessionInfo.getLastHeartbeat() > originalHeartbeat);
  }
}
