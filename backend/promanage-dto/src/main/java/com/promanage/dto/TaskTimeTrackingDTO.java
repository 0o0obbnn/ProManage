package com.promanage.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务时间追踪DTO
 *
 * <p>用于传输任务时间追踪相关数据
 *
 * @author ProManage Team
 * @since 2025-10-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "任务时间追踪DTO")
@SuppressWarnings("PMD.TooManyFields")
public class TaskTimeTrackingDTO {

  /** 主键ID */
  @Schema(description = "主键ID", example = "1")
  private Long id;

  /** 任务ID */
  @Schema(description = "任务ID", example = "100")
  private Long taskId;

  /** 任务名称 */
  @Schema(description = "任务名称", example = "实现用户登录功能")
  private String taskName;

  /** 用户ID */
  @Schema(description = "用户ID", example = "10")
  private Long userId;

  /** 用户名 */
  @Schema(description = "用户名", example = "zhangsan")
  private String username;

  /** 用户真实姓名 */
  @Schema(description = "用户真实姓名", example = "张三")
  private String userRealName;

  /** 预估工时 */
  @Schema(description = "预估工时(小时)", example = "8.0")
  private Double estimatedHours;

  /** 实际工时 */
  @Schema(description = "实际工时(小时)", example = "6.5")
  private Double actualHours;

  /** 工作日期 */
  @Schema(description = "工作日期", example = "2025-10-05")
  private LocalDate workDate;

  /** 工作描述 */
  @Schema(description = "工作描述", example = "完成了登录界面UI开发")
  private String workDescription;

  /**
   * 状态
   *
   * <ul>
   *   <li>0 - 未开始
   *   <li>1 - 进行中
   *   <li>2 - 已完成
   *   <li>3 - 已暂停
   * </ul>
   */
  @Schema(description = "状态 (0-未开始, 1-进行中, 2-已完成, 3-已暂停)", example = "1")
  private Integer status;

  /** 开始时间 */
  @Schema(description = "开始时间", example = "2025-10-05T09:00:00")
  private LocalDateTime startTime;

  /** 结束时间 */
  @Schema(description = "结束时间", example = "2025-10-05T17:30:00")
  private LocalDateTime endTime;

  /** 创建时间 */
  @Schema(description = "创建时间", example = "2025-10-05T09:00:00")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Schema(description = "更新时间", example = "2025-10-05T17:30:00")
  private LocalDateTime updateTime;

  /** 项目ID（关联查询） */
  @Schema(description = "项目ID", example = "5")
  private Long projectId;

  /** 项目名称（关联查询） */
  @Schema(description = "项目名称", example = "ProManage系统开发")
  private String projectName;
}
