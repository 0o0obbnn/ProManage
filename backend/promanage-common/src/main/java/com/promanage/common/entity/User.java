package com.promanage.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 *
 * <p>用户信息实体，包含用户的基本信息和状态
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_user")
@Schema(description = "用户信息")
public class User extends BaseEntity {

  /** 用户名 */
  @Schema(description = "用户名", example = "admin")
  private String username;

  /** 密码（加密后） */
  @Schema(description = "密码", example = "encrypted_password")
  private String password;

  /** 邮箱 */
  @Schema(description = "邮箱", example = "admin@promanage.com")
  private String email;

  /** 手机号 */
  @Schema(description = "手机号", example = "13800138000")
  private String phone;

  /** 头像URL */
  @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
  private String avatar;

  /** 用户状态 0-禁用 1-启用 2-锁定 */
  @Schema(description = "用户状态", example = "1")
  private Integer status;

  /** 真实姓名 */
  @Schema(description = "真实姓名", example = "管理员")
  private String realName;

  /** 部门ID */
  @Schema(description = "部门ID", example = "1")
  private Long departmentId;

  /** 组织ID */
  @Schema(description = "组织ID", example = "1")
  private Long organizationId;

  /** 职位 */
  @Schema(description = "职位", example = "系统管理员")
  private String position;

  /** 最后登录时间 */
  @Schema(description = "最后登录时间")
  private java.time.LocalDateTime lastLoginTime;

  /** 最后登录IP */
  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  @Schema(description = "最后登录IP", example = "192.168.1.100")
  private String lastLoginIp;
}
