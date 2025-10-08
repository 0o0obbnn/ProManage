package com.promanage.api.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promanage.common.websocket.WebSocketMessage;
import com.promanage.api.websocket.WebSocketSessionManager.WebSocketSessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;

/**
 * 通知WebSocket处理器
 */
@Component
public class NotificationWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final WebSocketSessionManager sessionManager;
    
    public NotificationWebSocketHandler(ObjectMapper objectMapper, WebSocketSessionManager sessionManager) {
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
    }

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
    public void handleMessage(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
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
                    WebSocketSessionInfo sessionInfo = sessionManager.getUserSessionInfo(onlineUserId);
                    if (sessionInfo != null && sessionId.equals(sessionInfo.getSessionId())) {
                        userId = onlineUserId;
                        break;
                    }
                }
                
                if (userId != null) {
                    WebSocketSessionInfo sessionInfo = sessionManager.getUserSessionInfo(userId);
                    if (sessionInfo != null) {
                        sessionInfo.getHandler().sendMessage(WebSocketMessage.heartbeat());
                    }
                }
                return;
            }
            
            // 这里可以处理其他类型的客户端消息
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败, 会话ID: {}, 错误: {}", sessionId, e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket传输错误, 会话ID: {}, 错误: {}", 
                sessionId, exception.getMessage(), exception);
        
        // 清理会话
        sessionManager.removeUserSession(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        log.info("会话 {} 关闭WebSocket连接, 状态: {}", 
                sessionId, closeStatus);
        
        // 清理会话
        sessionManager.removeUserSession(sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 向指定用户发送消息
     */
    public boolean sendMessageToUser(Long userId, WebSocketMessage message) {
        return sessionManager.sendMessageToUser(userId, message);
    }

    /**
     * 向所有用户广播消息
     */
    public void broadcastMessage(WebSocketMessage message) {
        sessionManager.broadcastMessage(message);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return sessionManager.isUserOnline(userId);
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return sessionManager.getOnlineUserCount();
    }

    /**
     * 从会话中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 从查询参数中获取token
            String query = session.getUri().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        String token = param.substring(6);
                        // 这里应该解析JWT token获取用户ID
                        // 简化实现，实际应该使用JWT工具类
                        return parseUserIdFromToken(token);
                    }
                }
            }
        } catch (Exception e) {
            log.error("从会话获取用户ID失败: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从token解析用户ID（简化实现）
     */
    private Long parseUserIdFromToken(String token) {
        // 实际应该使用JWT工具类解析token
        // 这里只是示例实现
        try {
            // 假设token格式为 "userId:xxx"
            if (token.startsWith("userId:")) {
                return Long.parseLong(token.substring(7));
            }
        } catch (NumberFormatException e) {
            log.error("解析用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}