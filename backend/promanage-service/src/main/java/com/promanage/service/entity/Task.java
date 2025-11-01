package com.promanage.service.entity;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务实体类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task")
@Schema(description = "任务信息")
public class Task extends BaseEntity {

  /** 任务标题 */
  @Schema(description = "任务标题", example = "实现用户登录功能")
  private String title;

  /** 任务描述 */
  @Schema(description = "任务描述", example = "实现用户登录的前端界面和后端API")
  private String description;

  /** 任务状态 0-待办, 1-进行中, 2-审核中, 3-已完成, 4-已取消, 5-已阻塞 */
  @Schema(description = "任务状态 (0-待办, 1-进行中, 2-审核中, 3-已完成, 4-已取消, 5-已阻塞)", example = "0")
  private Integer status;

  /** 任务优先级 1-低, 2-中, 3-高, 4-紧急 */
  @Schema(description = "任务优先级 (1-低, 2-中, 3-高, 4-紧急)", example = "2")
  private Integer priority;

  /** 指派人ID */
  @Schema(description = "指派人ID", example = "1")
  private Long assigneeId;

  /** 报告人ID */
  @Schema(description = "报告人ID", example = "2")
  private Long reporterId;

  /** 父任务ID */
  @Schema(description = "父任务ID", example = "")
  private Long parentTaskId;

  /** 项目ID */
  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  /** 预估工时（小时） */
  @Schema(description = "预估工时（小时）", example = "16.0")
  private Double estimatedHours;

  /** 已用工时（小时） */
  @Schema(description = "已用工时（小时）", example = "4.0")
  private Double actualHours;

  /** 完成进度百分比（0-100） */
  @Schema(description = "完成进度百分比（0-100）", example = "75")
  private Integer progressPercentage;

  /** 开始日期 */
  @Schema(description = "开始日期", example = "2025-10-01")
  private LocalDate startDate;

  /** 截止日期 */
  @Schema(description = "截止日期", example = "2025-10-07")
  private LocalDate dueDate;

  /** 完成日期 */
  @Schema(description = "完成日期", example = "2025-10-06")
  private LocalDate completedDate;

  /** 任务标签，多个标签用逗号分隔 */
  @Schema(description = "任务标签，多个标签用逗号分隔", example = "前端,登录,UI")
  private String tags;
}
