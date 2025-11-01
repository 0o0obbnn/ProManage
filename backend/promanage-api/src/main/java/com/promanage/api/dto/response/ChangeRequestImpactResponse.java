package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 变更请求影响分析响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "变更请求影响分析响应")
public class ChangeRequestImpactResponse {
  @Schema(description = "影响ID", example = "1")
  private Long id;

  @Schema(description = "受影响实体类型", example = "DOCUMENT")
  private String entityType;

  @Schema(description = "受影响实体ID", example = "123")
  private Long entityId;

  @Schema(description = "受影响实体标题", example = "用户登录界面设计文档")
  private String entityTitle;

  @Schema(description = "影响程度 (LOW-低, MEDIUM-中, HIGH-高, CRITICAL-关键)", example = "MEDIUM")
  private String impactLevel;

  @Schema(description = "影响描述", example = "界面重新设计会影响此文档的结构")
  private String impactDescription;

  @Schema(description = "置信度分数 (0.0-1.0)", example = "0.85")
  private Double confidenceScore;

  @Schema(description = "是否已人工验证", example = "false")
  private Boolean isVerified;

  @Schema(description = "验证人姓名")
  private String verifiedBy;

  @Schema(description = "验证时间")
  private LocalDateTime verifiedAt;
}
