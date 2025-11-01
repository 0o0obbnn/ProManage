package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 *
 * <p>用于新用户注册账号，包含基本身份信息和联系方式
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

  /**
   * 用户名
   *
   * <p>必填项，长度限制为3-50个字符 用户名必须唯一，只能包含字母、数字和下划线
   */
  @NotBlank(message = "用户名不能为空")
  @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
  @Schema(description = "用户名", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  /**
   * 密码
   *
   * <p>必填项，长度限制为6-100个字符 建议前端强制要求包含大小写字母、数字和特殊字符
   */
  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
  @Schema(
      description = "密码",
      example = "SecurePass@123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;

  /**
   * 确认密码
   *
   * <p>必填项，必须与密码字段一致
   */
  @NotBlank(message = "确认密码不能为空")
  @Schema(
      description = "确认密码",
      example = "SecurePass@123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String confirmPassword;

  /**
   * 电子邮箱
   *
   * <p>必填项，用于账号验证和密码找回 邮箱地址必须唯一
   */
  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  @Schema(
      description = "电子邮箱",
      example = "john.doe@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  /**
   * 手机号码
   *
   * <p>可选项，支持中国大陆手机号格式
   */
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  @Schema(description = "手机号码", example = "13800138000")
  private String phone;

  /**
   * 真实姓名
   *
   * <p>可选项，最大长度50个字符
   */
  @Size(max = 50, message = "真实姓名长度不能超过50个字符")
  @Schema(description = "真实姓名", example = "张三")
  private String realName;

  /**
   * 邮箱验证码
   *
   * <p>可选项，如果启用邮箱验证，则为必填
   */
  @Schema(description = "邮箱验证码", example = "123456")
  private String verificationCode;
}
