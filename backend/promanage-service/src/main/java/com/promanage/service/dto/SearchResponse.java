package com.promanage.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 搜索响应DTO */
@Data
@Schema(description = "搜索响应")
public class SearchResponse {

  @Schema(description = "类型: document/project/task")
  private String type;

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "标题")
  private String title;

  @Schema(description = "内容")
  private String content;

  @Schema(description = "高亮内容")
  private String highlightedContent;

  @Schema(description = "作者")
  private String author;

  @Schema(description = "创建时间")
  private String createdTime;

  @Schema(description = "更新时间")
  private String updatedTime;

  @Schema(description = "项目ID（如果有）")
  private Long projectId;

  @Schema(description = "项目名称（如果有）")
  private String projectName;
}
