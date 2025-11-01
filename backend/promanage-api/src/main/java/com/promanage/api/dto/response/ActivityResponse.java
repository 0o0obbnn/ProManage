package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 活动日志响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动日志响应")
public class ActivityResponse {

  @Schema(description = "活动ID", example = "1")
  private Long id;

  @Schema(description = "操作用户ID", example = "1")
  private Long userId;

  @Schema(description = "操作用户姓名", example = "张三")
  private String userName;

  @Schema(description = "操作用户头像", example = "https://example.com/avatar/user1.jpg")
  private String userAvatar;

  @Schema(description = "操作类型", example = "CREATE")
  private String action;

  @Schema(description = "实体类型", example = "TASK")
  private String entityType;

  @Schema(description = "实体ID", example = "123")
  private Long entityId;

  @Schema(description = "实体标题", example = "实现用户登录功能")
  private String entityTitle;

  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  @Schema(description = "项目名称", example = "ProManage系统开发")
  private String projectName;

  @Schema(description = "操作描述", example = "创建了新任务")
  private String description;

  @Schema(description = "创建时间", example = "2025-10-02T14:30:00")
  private LocalDateTime createTime;

  @Schema(description = "额外数据（JSON格式）")
  private String metadata;

  @Schema(description = "是否为用户自己的活动", example = "true")
  private Boolean isOwnActivity;

  @Schema(description = "活动图标", example = "task-create")
  private String icon;

  @Schema(description = "活动颜色", example = "#52c41a")
  private String color;

  @Schema(description = "活动类型描述", example = "任务创建")
  private String actionDescription;

  @Schema(description = "时间描述（相对时间）", example = "2小时前")
  private String timeAgo;
}
