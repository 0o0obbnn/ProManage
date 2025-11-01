package com.promanage.service.dto.request;

import java.util.List;

import lombok.Data;

/** 发送通知请求DTO */
@Data
public class NotificationSendRequest {

  /** 接收者ID列表 */
  private List<Long> userIds;

  /** 通知类型 */
  private String type;

  /** 通知标题 */
  private String title;

  /** 通知内容 */
  private String content;

  /** 相关数据ID */
  private Long relatedId;

  /** 相关数据类型 */
  private String relatedType;
}
