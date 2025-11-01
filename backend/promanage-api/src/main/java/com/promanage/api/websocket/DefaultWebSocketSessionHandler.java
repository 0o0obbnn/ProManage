package com.promanage.api.websocket;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.promanage.common.websocket.WebSocketMessage;

/** 默认WebSocket会话处理器实现 */
public class DefaultWebSocketSessionHandler implements WebSocketSessionHandler {

  private static final Logger log = LoggerFactory.getLogger(DefaultWebSocketSessionHandler.class);

  private final WebSocketSession session;
  private final ObjectMapper objectMapper;

  public DefaultWebSocketSessionHandler(WebSocketSession session, ObjectMapper objectMapper) {
    this.session = session;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean sendMessage(WebSocketMessage message) {
    try {
      if (session.isOpen()) {
        String jsonMessage = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(jsonMessage));
        return true;
      }
    } catch (IOException e) {
      log.error("发送WebSocket消息失败, 会话ID: {}, 错误: {}", session.getId(), e.getMessage(), e);
    }
    return false;
  }

  @Override
  public boolean isActive() {
    return session.isOpen();
  }

  @Override
  public void close() {
    try {
      if (session.isOpen()) {
        session.close();
      }
    } catch (IOException e) {
      log.error("关闭WebSocket会话失败, 会话ID: {}, 错误: {}", session.getId(), e.getMessage(), e);
    }
  }

  @Override
  public String getSessionId() {
    return session.getId();
  }
}
