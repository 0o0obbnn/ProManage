package com.promanage.service.strategy;

import org.springframework.stereotype.Component;

import com.promanage.service.strategy.impl.ProjectNotificationStrategy;
import com.promanage.service.strategy.impl.TaskNotificationStrategy;

import lombok.RequiredArgsConstructor;

/** 通知策略工厂 */
@Component
@RequiredArgsConstructor
public class NotificationStrategyFactory {

  private final ProjectNotificationStrategy projectNotificationStrategy;
  private final TaskNotificationStrategy taskNotificationStrategy;

  /** 根据相关类型获取通知策略 */
  public NotificationStrategy getStrategy(String relatedType) {
    return switch (relatedType) {
      case "PROJECT_CREATED",
          "PROJECT_UPDATED",
          "PROJECT_MEMBER_ADDED",
          "PROJECT_MEMBER_REMOVED" -> projectNotificationStrategy;
      case "TASK_CREATED",
          "TASK_UPDATED",
          "TASK_ASSIGNED",
          "TASK_COMPLETED",
          "TASK_OVERDUE" -> taskNotificationStrategy;
      default -> null;
    };
  }
}
