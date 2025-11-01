package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送密码重置验证码请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
@Data
@Schema(description = "发送密码重置验证码请求")
public class SendResetCodeRequest {

  /** 电子邮箱 */
  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  @Schema(
      description = "电子邮箱",
      example = "user@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;
}
