package com.promanage.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 密码强度响应
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "密码强度响应")
public class PasswordStrengthResponse {

  @Schema(description = "强度等级: weak, medium, strong", example = "medium")
  private String level;

  @Schema(description = "强度百分比 (0-100)", example = "60")
  private Integer percent;

  @Schema(description = "强度文本描述", example = "中")
  private String text;

  @Schema(description = "是否满足最低要求", example = "true")
  private Boolean meetsRequirements;

  @Schema(description = "改进建议")
  private String suggestion;
}
