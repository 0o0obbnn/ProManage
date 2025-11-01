package com.promanage.service.dto.request;

import java.util.List;

import lombok.Data;

/** 通知操作请求DTO */
@Data
public class NotificationActionRequest {

  /** 通知ID列表 */
  private List<Long> notificationIds;

  /** 操作类型：read-标记已读，delete-删除 */
  private String action;
}
