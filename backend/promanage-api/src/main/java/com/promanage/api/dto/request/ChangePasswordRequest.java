package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求DTO
 *
 * <p>用于已登录用户修改自己的密码 需要验证旧密码的正确性
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

  /**
   * 旧密码
   *
   * <p>必填项，用于验证用户身份
   */
  @NotBlank(message = "旧密码不能为空")
  @Schema(description = "旧密码", example = "OldPass@123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String oldPassword;

  /**
   * 新密码
   *
   * <p>必填项，长度限制为6-100个字符 不能与旧密码相同
   */
  @NotBlank(message = "新密码不能为空")
  @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
  @Schema(description = "新密码", example = "NewPass@123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String newPassword;

  /**
   * 确认新密码
   *
   * <p>必填项，必须与新密码字段一致
   */
  @NotBlank(message = "确认密码不能为空")
  @Schema(
      description = "确认新密码",
      example = "NewPass@123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String confirmPassword;
}
