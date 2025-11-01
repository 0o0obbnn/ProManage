package com.promanage.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 *
 * <p>存储系统角色信息,用于权限管理和访问控制
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_role")
@Schema(description = "角色实体")
public class Role extends BaseEntity {

  private static final long serialVersionUID = 1L;

  /** 角色名称 (不能为空) */
  @TableField("role_name")
  @Schema(description = "角色名称", example = "项目经理", required = true)
  private String roleName;

  /**
   * 角色编码 (唯一, 不能为空)
   *
   * <p>格式: ROLE_XXX 例如: ROLE_ADMIN, ROLE_PM, ROLE_DEVELOPER
   */
  @TableField("role_code")
  @Schema(description = "角色编码", example = "ROLE_PM", required = true)
  private String roleCode;

  /** 角色描述 */
  @TableField("description")
  @Schema(description = "角色描述", example = "负责项目管理和团队协调")
  private String description;

  /** 排序 (数值越小越靠前) */
  @TableField("sort")
  @Schema(description = "排序", example = "1", defaultValue = "0")
  private Integer sort;

  /**
   * 角色状态
   *
   * <ul>
   *   <li>0 - 正常
   *   <li>1 - 禁用
   * </ul>
   */
  @TableField("status")
  @Schema(description = "角色状态 (0-正常, 1-禁用)", example = "0", defaultValue = "0")
  private Integer status;
}
