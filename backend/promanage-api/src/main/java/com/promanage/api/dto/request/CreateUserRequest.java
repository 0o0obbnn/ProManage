package com.promanage.api.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户请求DTO
 *
 * <p>用于管理员创建新用户账号，包含完整的用户信息和角色分配
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "创建用户请求")
public class CreateUserRequest {

  /**
   * 用户名
   *
   * <p>必填项，长度限制为3-50个字符 用户名必须唯一，只能包含字母、数字和下划线
   */
  @NotBlank(message = "用户名不能为空")
  @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
  @Schema(description = "用户名", example = "developer01", requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  /**
   * 密码
   *
   * <p>必填项，长度限制为6-100个字符 后端会使用BCrypt进行加密存储
   */
  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
  @Schema(description = "密码", example = "User@123456", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;

  /**
   * 电子邮箱
   *
   * <p>必填项，邮箱地址必须唯一
   */
  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  @Schema(
      description = "电子邮箱",
      example = "developer@example.com",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  /**
   * 手机号码
   *
   * <p>可选项，支持中国大陆手机号格式
   */
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  @Schema(description = "手机号码", example = "13900139000")
  private String phone;

  /**
   * 真实姓名
   *
   * <p>可选项，最大长度50个字符
   */
  @Size(max = 50, message = "真实姓名长度不能超过50个字符")
  @Schema(description = "真实姓名", example = "李开发")
  private String realName;

  /**
   * 角色ID列表
   *
   * <p>可选项，为用户分配的角色ID集合 如果不指定，默认分配"普通用户"角色
   */
  @Schema(description = "角色ID列表", example = "[2, 3]")
  private List<Long> roleIds;

  /**
   * 用户状态
   *
   * <p>可选项，0-禁用，1-正常（默认），2-锁定
   */
  @Schema(description = "用户状态：0-禁用，1-正常，2-锁定", example = "1")
  private Integer status;
}
