package com.promanage.api.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.promanage.common.websocket.WebSocketMessage;

import lombok.RequiredArgsConstructor;

/** 通知WebSocket处理器 */
@RequiredArgsConstructor
@Component
public class NotificationWebSocketHandler implements WebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

  private final ObjectMapper objectMapper;
  private final WebSocketSessionManager sessionManager;
  private final WebSocketAuthService webSocketAuthService;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // 从会话属性中获取用户ID
    Long userId = getUserIdFromSession(session);
    if (userId != null) {
      // 创建会话处理器
      WebSocketSessionHandler handler = new DefaultWebSocketSessionHandler(session, objectMapper);

      // 添加到会话管理器
      sessionManager.addUserSession(userId, session.getId(), handler);

      log.info("用户 {} 建立WebSocket连接, 会话ID: {}", userId, session.getId());

      // 发送连接成功消息
      handler.sendMessage(WebSocketMessage.system("连接成功", "WebSocket连接已建立"));
    } else {
      log.warn("无法从会话中获取用户ID, 关闭连接");
      session.close();
    }
  }

  @Override
  public void handleMessage(
      WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message)
      throws Exception {
    String sessionId = session.getId();
    try {
      String payload = message.getPayload().toString();
      log.debug("收到会话 {} 的消息: {}", sessionId, payload);

      // 处理心跳消息
      if ("heartbeat".equals(payload)) {
        sessionManager.updateHeartbeat(sessionId);
        // 通过sessionId找到对应的userId
        Long userId = null;
        for (Long onlineUserId : sessionManager.getOnlineUserIds()) {
          WebSocketSessionManager.WebSocketSessionInfo sessionInfo =
              sessionManager.getUserSessionInfo(onlineUserId);
          if (sessionInfo != null && sessionId.equals(sessionInfo.getSessionId())) {
            userId = onlineUserId;
            break;
          }
        }

        if (userId != null) {
          WebSocketSessionManager.WebSocketSessionInfo sessionInfo =
              sessionManager.getUserSessionInfo(userId);
          if (sessionInfo != null) {
            boolean sent = sessionInfo.getHandler().sendMessage(WebSocketMessage.heartbeat());
            if (!sent) {
              log.warn("发送心跳消息失败, 会话ID: {}", sessionId);
              sessionManager.removeUserSession(sessionId);
            }
          }
        }
        return;
      }

      // 这里可以处理其他类型的客户端消息

    } catch (NullPointerException e) {
      log.error("处理WebSocket消息失败, 会话ID: {}, 空指针异常: {}", sessionId, e.getMessage(), e);
      WebSocketSessionHandler handler =
          new DefaultWebSocketSessionHandler(session, objectMapper);
      boolean sent = handler.sendMessage(WebSocketMessage.error("消息格式错误"));
      if (!sent) {
        log.error("发送错误消息失败, 会话ID: {}", sessionId);
        sessionManager.removeUserSession(sessionId);
      }
    } catch (IllegalArgumentException e) {
      log.warn("参数错误, 会话ID: {}, 错误: {}", sessionId, e.getMessage());
      WebSocketSessionHandler handler =
          new DefaultWebSocketSessionHandler(session, objectMapper);
      boolean sent = handler.sendMessage(WebSocketMessage.error("参数错误: " + e.getMessage()));
      if (!sent) {
        log.error("发送错误消息失败, 会话ID: {}", sessionId);
        sessionManager.removeUserSession(sessionId);
      }
    } catch (RuntimeException e) {
      log.error("处理WebSocket消息失败, 会话ID: {}", sessionId, e);
      throw e;
    }
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    String sessionId = session.getId();
    log.error("WebSocket传输错误, 会话ID: {}, 错误: {}", sessionId, exception.getMessage(), exception);

    // 清理会话
    sessionManager.removeUserSession(sessionId);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
      throws Exception {
    String sessionId = session.getId();
    log.info("会话 {} 关闭WebSocket连接, 状态: {}", sessionId, closeStatus);

    // 清理会话
    sessionManager.removeUserSession(sessionId);
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }

  /** 向指定用户发送消息 */
  public boolean sendMessageToUser(Long userId, WebSocketMessage message) {
    return sessionManager.sendMessageToUser(userId, message);
  }

  /** 向所有用户广播消息 */
  public void broadcastMessage(WebSocketMessage message) {
    sessionManager.broadcastMessage(message);
  }

  /** 检查用户是否在线 */
  public boolean isUserOnline(Long userId) {
    return sessionManager.isUserOnline(userId);
  }

  /** 获取在线用户数量 */
  public int getOnlineUserCount() {
    return sessionManager.getOnlineUserCount();
  }

  /** 从会话中获取用户ID */
  private Long getUserIdFromSession(WebSocketSession session) {
    try {
      // 从查询参数中获取token
      if (session.getUri() == null) {
        log.warn("会话URI为null, 无法获取用户ID");
        return null;
      }

      String query = session.getUri().getQuery();
      if (query == null || query.trim().isEmpty()) {
        log.warn("会话查询参数为空, 无法获取Token");
        return null;
      }

      String[] params = query.split("&");
      for (String param : params) {
        if (param.startsWith("token=")) {
          String token = param.substring(6);
          // 使用WebSocketAuthService解析JWT token获取用户ID
          return webSocketAuthService.parseUserIdFromToken(token);
        }
      }

      log.warn("会话查询参数中未找到Token");
      return null;
    } catch (NullPointerException e) {
      log.error("从会话获取用户ID失败, URI或查询参数为null: {}", e.getMessage(), e);
      return null;
    } catch (IllegalArgumentException e) {
      log.error("从会话获取用户ID失败, 参数错误: {}", e.getMessage(), e);
      return null;
    } catch (RuntimeException e) {
      log.error("从会话获取用户ID失败: {}", e.getMessage(), e);
      return null;
    }
  }
}
