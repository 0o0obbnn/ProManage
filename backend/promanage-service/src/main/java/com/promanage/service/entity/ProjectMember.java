package com.promanage.service.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目成员实体类
 *
 * <p>存储项目团队成员信息和角色分配 一个项目可以有多个成员,一个用户可以参与多个项目
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_project_member")
@Schema(description = "项目成员实体")
public class ProjectMember extends BaseEntity {

  private static final long serialVersionUID = 1L;

  /** 项目ID (不能为空) */
  @TableField("project_id")
  @Schema(description = "项目ID", example = "1", required = true)
  private Long projectId;

  /** 用户ID (不能为空) */
  @TableField("user_id")
  @Schema(description = "用户ID", example = "1", required = true)
  private Long userId;

  /**
   * 项目中的角色ID (不能为空)
   *
   * <p>定义成员在项目中的角色 例如: 项目经理、开发人员、测试人员等
   */
  @TableField("role_id")
  @Schema(description = "项目角色ID", example = "1", required = true)
  private Long roleId;

  /** 加入项目时间 */
  @TableField("join_time")
  @Schema(description = "加入项目时间", example = "2025-09-30 10:00:00")
  private LocalDateTime joinTime;

  /**
   * 成员状态
   *
   * <ul>
   *   <li>0 - 正常
   *   <li>1 - 已退出
   * </ul>
   */
  @TableField("status")
  @Schema(description = "成员状态 (0-正常, 1-已退出)", example = "0", defaultValue = "0")
  private Integer status;

  /** 用户名 (transient field for service layer use) */
  @TableField(exist = false)
  @Schema(description = "用户名")
  private String username;

  /** 真实姓名 (transient field for service layer use) */
  @TableField(exist = false)
  @Schema(description = "真实姓名")
  private String realName;

  /** 角色名称 (transient field for service layer use) */
  @TableField(exist = false)
  @Schema(description = "角色名称")
  private String role;
}
