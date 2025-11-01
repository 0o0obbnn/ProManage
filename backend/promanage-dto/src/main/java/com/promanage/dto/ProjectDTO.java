package com.promanage.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目信息")
public class ProjectDTO {

  @Schema(description = "项目ID", example = "1")
  private Long id;

  @Schema(description = "项目名称", example = "ProManage 后端重构")
  private String name;

  @Schema(description = "项目编码", example = "PROMANAGE_REFACTOR")
  private String code;

  @Schema(description = "项目描述", example = "完成后端服务的多租户重构")
  private String description;

  @Schema(description = "项目状态 (0-规划中,1-进行中,2-已完成,3-已归档)", example = "1")
  private Integer status;

  @Schema(description = "所属组织ID", example = "1")
  private Long organizationId;

  @Schema(description = "项目负责人ID", example = "10")
  private Long ownerId;

  @Schema(description = "项目负责人姓名", example = "张三")
  private String ownerName;

  @Schema(description = "计划开始日期", example = "2025-10-15")
  private LocalDate startDate;

  @Schema(description = "计划结束日期", example = "2025-12-31")
  private LocalDate endDate;

  @Schema(description = "项目优先级 (1-低, 2-中, 3-高, 4-紧急)", example = "2")
  private Integer priority;

  @Schema(description = "项目进度百分比", example = "45")
  private Integer progress;

  @Schema(description = "项目标签，逗号分隔", example = "后端,重构")
  private String tags;

  @Schema(description = "项目封面图片URL", example = "https://example.com/project-cover.jpg")
  private String coverImage;

  @Schema(description = "项目图标", example = "project-icon.png")
  private String icon;

  @Schema(description = "项目主题色", example = "#1890FF")
  private String color;

  @Schema(description = "项目类型", example = "SOFTWARE")
  private String type;

  @Schema(description = "创建时间", example = "2025-10-10T12:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "更新时间", example = "2025-10-11T09:30:00")
  private LocalDateTime updatedAt;

  @Schema(description = "是否归档", example = "false")
  private Boolean archived;
}
