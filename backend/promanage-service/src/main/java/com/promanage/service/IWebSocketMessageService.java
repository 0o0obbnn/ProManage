package com.promanage.service;

import java.util.Collection;

/** WebSocket消息服务接口 */
public interface IWebSocketMessageService {

  /** 向指定用户发送消息 */
  boolean sendNotificationToUser(Long userId, String title, String content);

  /** 向指定用户发送消息（带相关数据） */
  boolean sendNotificationToUser(
      Long userId, String title, String content, Long relatedId, String relatedType);

  /** 向多个用户发送消息 */
  void sendNotificationToUsers(Collection<Long> userIds, String title, String content);

  /** 向多个用户发送消息（带相关数据） */
  void sendNotificationToUsers(
      Collection<Long> userIds, String title, String content, Long relatedId, String relatedType);

  /** 广播消息 */
  void broadcastNotification(String title, String content);

  /** 广播消息（带相关数据） */
  void broadcastNotification(String title, String content, Long relatedId, String relatedType);

  /** 向项目成员发送消息 */
  void sendProjectNotification(Long projectId, String title, String content);
}
