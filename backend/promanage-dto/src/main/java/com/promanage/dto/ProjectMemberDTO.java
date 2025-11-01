package com.promanage.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目成员DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@Schema(description = "项目成员信息")
public class ProjectMemberDTO {

  /** 成员ID */
  @Schema(description = "成员ID", example = "1")
  private Long id;

  /** 用户ID */
  @Schema(description = "用户ID", example = "1")
  private Long userId;

  /** 用户名 */
  @Schema(description = "用户名", example = "admin")
  private String username;

  /** 真实姓名 */
  @Schema(description = "真实姓名", example = "管理员")
  private String realName;

  /** 邮箱 */
  @Schema(description = "邮箱", example = "admin@promanage.com")
  private String email;

  /** 头像URL */
  @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
  private String avatar;

  /** 项目ID */
  @Schema(description = "项目ID", example = "1")
  private Long projectId;

  /** 角色ID */
  @Schema(description = "角色ID", example = "1")
  private Long roleId;

  /** 角色名称 */
  @Schema(description = "角色名称", example = "项目经理")
  private String roleName;

  /** 角色代码 */
  @Schema(description = "角色代码", example = "PROJECT_MANAGER")
  private String roleCode;

  /** 加入时间 */
  @Schema(description = "加入时间")
  private LocalDateTime joinTime;

  /** 创建时间 */
  @Schema(description = "创建时间")
  private LocalDateTime createTime;

  /** 状态 0-禁用 1-启用 */
  @Schema(description = "状态", example = "1")
  private Integer status;

  // ==================== Convenience methods ====================

  /**
   * Set role (convenience method that sets roleName)
   *
   * @param role Role name/code
   */
  public void setRole(String role) {
    this.roleName = role;
  }

  /**
   * Get role (convenience method that returns roleName)
   *
   * @return Role name
   */
  public String getRole() {
    return this.roleName;
  }
}
