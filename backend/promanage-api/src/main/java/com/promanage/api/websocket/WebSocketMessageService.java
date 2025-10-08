package com.promanage.api.websocket;

import com.promanage.common.websocket.WebSocketMessage;
import com.promanage.service.IWebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * WebSocket消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService implements IWebSocketMessageService {

    private final WebSocketSessionManager sessionManager;

    @Override
    public boolean sendNotificationToUser(Long userId, String title, String content) {
        WebSocketMessage message = WebSocketMessage.notification(title, content, null, null);
        return sessionManager.sendMessageToUser(userId, message);
    }

    @Override
    public boolean sendNotificationToUser(Long userId, String title, String content, Long relatedId, String relatedType) {
        WebSocketMessage message = WebSocketMessage.notification(title, content, relatedId, relatedType);
        return sessionManager.sendMessageToUser(userId, message);
    }

    @Override
    public void sendNotificationToUsers(Collection<Long> userIds, String title, String content) {
        WebSocketMessage message = WebSocketMessage.notification(title, content, null, null);
        sessionManager.sendMessageToUsers(userIds, message);
    }

    @Override
    public void sendNotificationToUsers(Collection<Long> userIds, String title, String content, Long relatedId, String relatedType) {
        WebSocketMessage message = WebSocketMessage.notification(title, content, relatedId, relatedType);
        sessionManager.sendMessageToUsers(userIds, message);
    }

    @Override
    public void broadcastNotification(String title, String content) {
        WebSocketMessage message = WebSocketMessage.notification(title, content, null, null);
        sessionManager.broadcastMessage(message);
    }

    @Override
    public void broadcastNotification(String title, String content, Long relatedId, String relatedType) {
        WebSocketMessage message = WebSocketMessage.notification(title, content, relatedId, relatedType);
        sessionManager.broadcastMessage(message);
    }

    @Override
    public void sendProjectNotification(Long projectId, String title, String content) {
        // 这里需要根据项目ID获取项目成员列表
        // 暂时简化实现
        WebSocketMessage message = WebSocketMessage.notification(title, content, projectId, "project");
        sessionManager.broadcastMessage(message);
        log.info("发送项目通知: 项目ID={}, 标题={}", projectId, title);
    }
}