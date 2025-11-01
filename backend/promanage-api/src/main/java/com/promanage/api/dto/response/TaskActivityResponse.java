package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 任务活动响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务活动响应")
public class TaskActivityResponse {

  @Schema(description = "活动ID", example = "1")
  private Long id;

  @Schema(description = "活动类型", example = "STATUS_CHANGE")
  private String activityType;

  @Schema(description = "活动内容/描述", example = "将任务状态从'进行中'变更为'已完成'")
  private String content;

  @Schema(description = "旧值", example = "进行中")
  private String oldValue;

  @Schema(description = "新值", example = "已完成")
  private String newValue;

  @Schema(description = "用户ID", example = "1")
  private Long userId;

  @Schema(description = "用户姓名", example = "张三")
  private String userName;

  @Schema(description = "用户头像", example = "https://example.com/avatar/user1.jpg")
  private String userAvatar;

  @Schema(description = "创建时间", example = "2025-10-02T14:30:00")
  private LocalDateTime createTime;
}
