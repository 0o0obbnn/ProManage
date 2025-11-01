package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 搜索响应DTO
 *
 * @author ProManage Team
 * @since 2025-10-06
 */
@Data
@Schema(description = "搜索响应")
public class SearchResponse {

  @Schema(description = "类型", example = "document")
  private String type;

  @Schema(description = "ID", example = "1")
  private Long id;

  @Schema(description = "标题", example = "项目需求文档")
  private String title;

  @Schema(description = "内容", example = "这是项目的需求描述...")
  private String content;

  @Schema(description = "高亮内容", example = "这是项目的<mark>需求</mark>描述...")
  private String highlightedContent;

  @Schema(description = "链接", example = "/documents/1")
  private String link;

  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  @Schema(description = "项目名称", example = "ProManage项目")
  private String projectName;

  @Schema(description = "创建时间", example = "2025-10-06T10:00:00")
  private LocalDateTime createdAt;
}
