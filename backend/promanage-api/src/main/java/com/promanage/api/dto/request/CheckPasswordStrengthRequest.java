package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 检查密码强度请求
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
@Data
@Schema(description = "检查密码强度请求")
public class CheckPasswordStrengthRequest {

  @NotBlank(message = "密码不能为空")
  @Schema(description = "待检查的密码", required = true, example = "MyPassword123!")
  private String password;
}
