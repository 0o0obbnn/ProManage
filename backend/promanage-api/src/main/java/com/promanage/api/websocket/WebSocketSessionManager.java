package com.promanage.api.websocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.promanage.common.websocket.WebSocketMessage;

import lombok.Builder;
import lombok.Data;

/** WebSocket会话管理器 */
@Component
public class WebSocketSessionManager {

  private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

  // 存储用户ID和WebSocket会话的映射
  private final Map<Long, WebSocketSessionInfo> userSessions = new ConcurrentHashMap<>();

  // 存储WebSocket会话和用户ID的映射
  private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();

  // 定时任务执行器，用于清理过期会话
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public WebSocketSessionManager() {
    // 每5分钟清理一次过期会话
    scheduler.scheduleAtFixedRate(this::cleanExpiredSessions, 5, 5, TimeUnit.MINUTES);
  }

  /** WebSocket会话信息 */
  @Data
  @Builder
  public static class WebSocketSessionInfo {
    private Long userId;
    private String sessionId;
    private WebSocketSessionHandler handler;
    private long connectTime;
    private long lastHeartbeat;
  }

  /** 添加用户会话 */
  public void addUserSession(Long userId, String sessionId, WebSocketSessionHandler handler) {
    WebSocketSessionInfo sessionInfo =
        WebSocketSessionInfo.builder()
            .userId(userId)
            .sessionId(sessionId)
            .handler(handler)
            .connectTime(System.currentTimeMillis())
            .lastHeartbeat(System.currentTimeMillis())
            .build();

    userSessions.put(userId, sessionInfo);
    sessionUsers.put(sessionId, userId);

    log.info("用户 {} 建立WebSocket连接, 会话ID: {}", userId, sessionId);
  }

  /** 移除用户会话 */
  public void removeUserSession(String sessionId) {
    Long userId = sessionUsers.remove(sessionId);
    if (userId != null) {
      userSessions.remove(userId);
      log.info("用户 {} 断开WebSocket连接, 会话ID: {}", userId, sessionId);
    }
  }

  /** 更新心跳时间 */
  public void updateHeartbeat(String sessionId) {
    Long userId = sessionUsers.get(sessionId);
    if (userId != null) {
      WebSocketSessionInfo sessionInfo = userSessions.get(userId);
      if (sessionInfo != null) {
        sessionInfo.setLastHeartbeat(System.currentTimeMillis());
      }
    }
  }

  /** 向指定用户发送消息 */
  public boolean sendMessageToUser(Long userId, WebSocketMessage message) {
    WebSocketSessionInfo sessionInfo = userSessions.get(userId);
    if (sessionInfo != null && sessionInfo.getHandler().isActive()) {
      return sessionInfo.getHandler().sendMessage(message);
    }
    log.debug("用户 {} 没有活跃的WebSocket连接", userId);
    return false;
  }

  /** 向多个用户发送消息 */
  public void sendMessageToUsers(Collection<Long> userIds, WebSocketMessage message) {
    userIds.forEach(userId -> sendMessageToUser(userId, message));
  }

  /** 向所有用户广播消息 */
  public void broadcastMessage(WebSocketMessage message) {
    userSessions
        .values()
        .forEach(
            sessionInfo -> {
              if (sessionInfo.getHandler().isActive()) {
                sessionInfo.getHandler().sendMessage(message);
              }
            });
  }

  /** 检查用户是否在线 */
  public boolean isUserOnline(Long userId) {
    WebSocketSessionInfo sessionInfo = userSessions.get(userId);
    return sessionInfo != null && sessionInfo.getHandler().isActive();
  }

  /** 获取在线用户数量 */
  public int getOnlineUserCount() {
    return (int)
        userSessions.values().stream()
            .filter(sessionInfo -> sessionInfo.getHandler().isActive())
            .count();
  }

  /** 获取所有在线用户ID */
  public Set<Long> getOnlineUserIds() {
    Set<Long> onlineUsers = new HashSet<>();
    userSessions
        .values()
        .forEach(
            sessionInfo -> {
              if (sessionInfo.getHandler().isActive()) {
                onlineUsers.add(sessionInfo.getUserId());
              }
            });
    return onlineUsers;
  }

  /** 获取用户会话信息 */
  public WebSocketSessionInfo getUserSessionInfo(Long userId) {
    return userSessions.get(userId);
  }

  /** 清理过期会话 */
  private void cleanExpiredSessions() {
    long currentTime = System.currentTimeMillis();
    long expireTime = 10 * 60 * 1000; // 10分钟超时

    List<Long> expiredUsers = new ArrayList<>();
    userSessions
        .values()
        .forEach(
            sessionInfo -> {
              if (currentTime - sessionInfo.getLastHeartbeat() > expireTime) {
                expiredUsers.add(sessionInfo.getUserId());
              }
            });

    expiredUsers.forEach(
        userId -> {
          WebSocketSessionInfo sessionInfo = userSessions.get(userId);
          if (sessionInfo != null) {
            log.info("清理过期会话, 用户: {}, 会话ID: {}", userId, sessionInfo.getSessionId());
            sessionUsers.remove(sessionInfo.getSessionId());
            userSessions.remove(userId);
            sessionInfo.getHandler().close();
          }
        });

    if (!expiredUsers.isEmpty()) {
      log.info("清理了 {} 个过期会话", expiredUsers.size());
    }
  }

  /** 关闭所有会话 */
  public void closeAllSessions() {
    userSessions
        .values()
        .forEach(
            sessionInfo -> {
              try {
                sessionInfo.getHandler().close();
              } catch (RuntimeException e) {
                log.error("关闭WebSocket会话失败, 会话ID: {}, 错误: {}", 
                    sessionInfo.getSessionId(), e.getMessage(), e);
              }
            });
    userSessions.clear();
    sessionUsers.clear();
    scheduler.shutdown();
  }
}
