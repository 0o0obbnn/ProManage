package com.promanage.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目统计数据DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@Schema(description = "项目统计数据")
public class ProjectStatsDTO {

  /** 项目ID */
  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  /** 任务统计 */
  @Schema(description = "任务统计")
  private Map<String, Integer> taskStats;

  /** 文档统计 */
  @Schema(description = "文档统计")
  private Map<String, Integer> documentStats;

  /** 变更请求统计 */
  @Schema(description = "变更请求统计")
  private Map<String, Integer> changeRequestStats;

  /** 活动统计 */
  @Schema(description = "活动统计")
  private Map<String, Integer> activityStats;

  /** 任务总数 */
  @Schema(description = "任务总数", example = "50")
  private Integer totalTasks;

  /** 已完成任务数 */
  @Schema(description = "已完成任务数", example = "30")
  private Integer completedTasks;

  /** 进行中任务数 */
  @Schema(description = "进行中任务数", example = "15")
  private Integer inProgressTasks;

  /** 待办任务数 */
  @Schema(description = "待办任务数", example = "5")
  private Integer pendingTasks;

  /** 项目成员数 */
  @Schema(description = "项目成员数", example = "10")
  private Integer memberCount;

  /** 文档总数 */
  @Schema(description = "文档总数", example = "25")
  private Integer totalDocuments;

  /** 变更请求数 */
  @Schema(description = "变更请求数", example = "3")
  private Integer changeRequests;

  /** 项目进度百分比 */
  @Schema(description = "项目进度百分比", example = "60")
  private Double progressPercentage;
}
