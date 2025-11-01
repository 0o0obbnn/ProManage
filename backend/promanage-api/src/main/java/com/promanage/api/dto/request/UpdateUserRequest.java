package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户请求DTO
 *
 * <p>用于更新用户的基本信息和状态 所有字段都是可选的，只更新提供的字段
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {

  /**
   * 电子邮箱
   *
   * <p>可选项，如果更新邮箱需要验证唯一性
   */
  @Email(message = "邮箱格式不正确")
  @Schema(description = "电子邮箱", example = "newemail@example.com")
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
   * 用户头像URL
   *
   * <p>可选项，存储头像图片的访问地址
   */
  @Size(max = 500, message = "头像URL长度不能超过500个字符")
  @Schema(description = "用户头像URL", example = "https://example.com/avatar/user123.jpg")
  private String avatar;

  /**
   * 用户状态
   *
   * <p>可选项，0-禁用，1-正常，2-锁定 只有管理员才能修改此字段
   */
  @Schema(description = "用户状态：0-禁用，1-正常，2-锁定", example = "1")
  private Integer status;

  /**
   * 个人简介
   *
   * <p>可选项，最大长度500个字符
   */
  @Size(max = 500, message = "个人简介长度不能超过500个字符")
  @Schema(description = "个人简介", example = "资深Java开发工程师，专注于后端开发")
  private String bio;

  /**
   * 所属部门
   *
   * <p>可选项，最大长度100个字符
   */
  @Size(max = 100, message = "部门名称长度不能超过100个字符")
  @Schema(description = "所属部门", example = "研发部")
  private String department;

  /**
   * 职位
   *
   * <p>可选项，最大长度100个字符
   */
  @Size(max = 100, message = "职位名称长度不能超过100个字符")
  @Schema(description = "职位", example = "高级开发工程师")
  private String position;
}
