package com.promanage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.promanage.service.strategy.NotificationStrategy;
import com.promanage.service.strategy.NotificationStrategyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 通知发送服务 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendService {

  private final INotificationService notificationService;
  private final NotificationStrategyFactory strategyFactory;

  /** 发送策略通知 */
  public boolean sendStrategyNotification(Long relatedId, String relatedType, Long operatorId) {
    try {
      NotificationStrategy strategy = strategyFactory.getStrategy(relatedType);
      if (strategy == null) {
        log.warn("未找到对应的通知策略, 相关类型: {}", relatedType);
        return false;
      }

      // 获取接收者列表
      List<Long> recipients = strategy.getRecipients(relatedId, relatedType, operatorId);
      if (recipients.isEmpty()) {
        log.info("没有找到通知接收者, 相关ID: {}, 相关类型: {}", relatedId, relatedType);
        return true;
      }

      // 生成标题和内容
      String title = strategy.generateTitle(relatedId, relatedType, operatorId);
      String content = strategy.generateContent(relatedId, relatedType, operatorId);

      // 批量发送通知
      boolean result =
          notificationService.sendNotificationBatch(
              recipients, relatedType, title, content, relatedId, relatedType, operatorId);

      if (result) {
        log.info(
            "策略通知发送成功, 相关ID: {}, 相关类型: {}, 接收者数量: {}", relatedId, relatedType, recipients.size());
      }

      return result;
    } catch (IllegalArgumentException e) {
      log.error("发送策略通知失败, 相关ID: {}, 相关类型: {}", relatedId, relatedType, e);
      return false;
    }
  }

  /** 发送自定义通知 */
  public boolean sendCustomNotification(
      List<Long> userIds,
      String type,
      String title,
      String content,
      Long relatedId,
      String relatedType,
      Long operatorId) {
    try {
      return notificationService.sendNotificationBatch(
          userIds, type, title, content, relatedId, relatedType, operatorId);
    } catch (IllegalArgumentException e) {
      log.error("发送自定义通知失败, 类型: {}, 标题: {}", type, title, e);
      return false;
    }
  }
}
