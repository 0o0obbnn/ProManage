package com.promanage.service.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务时间追踪实体类
 *
 * <p>记录任务的时间追踪信息，包括预估工时和实际工时
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_time_tracking")
@Schema(description = "任务时间追踪")
public class TaskTimeTracking extends BaseEntity {

  private static final long serialVersionUID = 1L;

  /** 任务ID */
  @TableField("task_id")
  @Schema(description = "任务ID", example = "1")
  private Long taskId;

  /** 用户ID */
  @TableField("user_id")
  @Schema(description = "用户ID", example = "1")
  private Long userId;

  /** 预估工时（小时） */
  @TableField("estimated_hours")
  @Schema(description = "预估工时（小时）", example = "8.5")
  private Double estimatedHours;

  /** 实际工时（小时） */
  @TableField("actual_hours")
  @Schema(description = "实际工时（小时）", example = "7.5")
  private Double actualHours;

  /** 工作日期 */
  @TableField("work_date")
  @Schema(description = "工作日期", example = "2025-10-05")
  private LocalDate workDate;

  /** 工作描述 */
  @TableField("work_description")
  @Schema(description = "工作描述", example = "完成了用户界面的设计")
  private String workDescription;

  /** 开始时间 */
  @TableField("start_time")
  @Schema(description = "开始时间", example = "2025-10-05T09:00:00")
  private LocalDateTime startTime;

  /** 结束时间 */
  @TableField("end_time")
  @Schema(description = "结束时间", example = "2025-10-05T17:30:00")
  private LocalDateTime endTime;

  /** 状态（0-未开始，1-进行中，2-已完成，3-已暂停） */
  @TableField("status")
  @Schema(description = "状态", example = "0")
  private Integer status;
}
