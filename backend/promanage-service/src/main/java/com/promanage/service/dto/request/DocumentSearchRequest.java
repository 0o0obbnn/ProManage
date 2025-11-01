package com.promanage.service.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 文档搜索请求 */
@Data
@Schema(description = "文档搜索请求")
public class DocumentSearchRequest {

  @Schema(description = "页码", example = "1")
  private Integer page = 1;

  @Schema(description = "每页大小", example = "10")
  private Integer pageSize = 10;

  @Schema(description = "项目ID")
  private Long projectId;

  @Schema(description = "文档状态：DRAFT, UNDER_REVIEW, APPROVED, ARCHIVED, DEPRECATED")
  private String status;

  @Schema(description = "搜索关键词")
  private String keyword;

  @Schema(description = "文件夹ID")
  private Long folderId;

  @Schema(description = "创建人ID")
  private Long creatorId;

  @Schema(description = "文档类型")
  private String type;

  @Schema(description = "分类ID")
  private Long categoryId;

  @Schema(description = "标签（逗号分隔）")
  private String tags;

  @Schema(description = "创建时间开始")
  private LocalDateTime startTime;

  @Schema(description = "创建时间结束")
  private LocalDateTime endTime;
}
