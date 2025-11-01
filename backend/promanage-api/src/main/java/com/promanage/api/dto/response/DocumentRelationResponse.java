package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档关联关系信息")
public class DocumentRelationResponse {

  @Schema(description = "关联关系ID", example = "501")
  private Long id;

  @JsonProperty("target_document")
  @Schema(description = "目标文档信息")
  private DocumentResponse targetDocument;

  @JsonProperty("relation_type")
  @Schema(description = "关联类型", example = "REFERENCES")
  private String relationType;

  @Schema(description = "关系描述", example = "参考测试用例文档")
  private String description;

  @JsonProperty("created_by")
  @Schema(description = "创建者信息")
  private DocumentUserSummary createdBy;

  @JsonProperty("created_at")
  @Schema(description = "创建时间", example = "2025-09-25T09:00:00")
  private LocalDateTime createdAt;
}
