package com.promanage.api.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 任务响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务响应")
public class TaskResponse {

  @Schema(description = "任务ID", example = "1")
  private Long id;

  @Schema(description = "任务标题", example = "实现用户登录功能")
  private String title;

  @Schema(description = "任务描述", example = "实现用户登录的前端界面和后端API")
  private String description;

  @Schema(description = "任务状态 (0-待办, 1-进行中, 2-审核中, 3-已完成, 4-已取消, 5-已阻塞)", example = "1")
  private Integer status;

  @Schema(description = "任务优先级 (1-低, 2-中, 3-高, 4-紧急)", example = "2")
  private Integer priority;

  @Schema(description = "指派人ID", example = "1")
  private Long assigneeId;

  @Schema(description = "指派人姓名", example = "张三")
  private String assigneeName;

  @Schema(description = "指派人头像", example = "https://example.com/avatar/user1.jpg")
  private String assigneeAvatar;

  @Schema(description = "报告人ID", example = "2")
  private Long reporterId;

  @Schema(description = "报告人姓名", example = "李四")
  private String reporterName;

  @Schema(description = "报告人头像", example = "https://example.com/avatar/user2.jpg")
  private String reporterAvatar;

  @Schema(description = "父任务ID", example = "")
  private Long parentTaskId;

  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  @Schema(description = "项目名称", example = "ProManage系统开发")
  private String projectName;

  @Schema(description = "预估工时（小时）", example = "8.0")
  private Double estimatedHours;

  @Schema(description = "实际工时（小时）", example = "6.5")
  private Double actualHours;

  @Schema(description = "完成进度百分比（0-100）", example = "75")
  private Integer progressPercentage;

  @Schema(description = "开始日期", example = "2025-10-01")
  private LocalDate startDate;

  @Schema(description = "截止日期", example = "2025-10-07")
  private LocalDate dueDate;

  @Schema(description = "完成日期", example = "2025-10-06")
  private LocalDate completedDate;

  @Schema(description = "任务标签，多个标签用逗号分隔", example = "前端,登录,UI")
  private String tags;

  @Schema(description = "评论数量", example = "5")
  private Integer commentCount;

  @Schema(description = "附件数量", example = "2")
  private Integer attachmentCount;

  @Schema(description = "子任务数量", example = "3")
  private Integer subtaskCount;

  @Schema(description = "创建时间", example = "2025-09-30T10:00:00")
  private LocalDateTime createTime;

  @Schema(description = "更新时间", example = "2025-10-02T15:30:00")
  private LocalDateTime updateTime;
}
