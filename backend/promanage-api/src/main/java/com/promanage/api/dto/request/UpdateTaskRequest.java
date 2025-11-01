package com.promanage.api.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新任务请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@Schema(description = "更新任务请求")
public class UpdateTaskRequest {

  @Size(max = 500, message = "任务标题长度不能超过500个字符")
  @Schema(description = "任务标题", example = "实现用户登录功能")
  private String title;

  @Size(max = 2000, message = "任务描述长度不能超过2000个字符")
  @Schema(description = "任务描述", example = "实现用户登录的前端界面和后端API")
  private String description;

  @Schema(description = "任务状态 (0-待办, 1-进行中, 2-审核中, 3-已完成, 4-已取消, 5-已阻塞)")
  private Integer status;

  @Schema(description = "任务优先级 (1-低, 2-中, 3-高, 4-紧急)")
  private Integer priority;

  @Schema(description = "指派人ID", example = "1")
  private Long assigneeId;

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

  @Schema(description = "任务标签数组", example = "[\"前端\", \"登录\", \"UI\"]")
  private String[] labels;
}
