package com.promanage.service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目成员DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberDTO {

  /** 成员ID */
  private Long id;

  /** 项目ID */
  private Long projectId;

  /** 用户ID */
  private Long userId;

  /** 用户名 */
  private String username;

  /** 真实姓名 */
  private String realName;

  /** 邮箱 */
  private String email;

  /** 角色ID */
  private Long roleId;

  /** 角色名称 */
  private String roleName;

  /** 加入时间 */
  private LocalDateTime joinTime;

  /** 状态 (0-正常, 1-禁用) */
  private Integer status;

  /** 创建人ID */
  private Long creatorId;

  /** 创建时间 */
  private LocalDateTime createTime;
}
