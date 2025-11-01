package com.promanage.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色权限关联实体类
 *
 * <p>映射角色和权限的多对多关系 一个角色可以拥有多个权限,一个权限可以分配给多个角色
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_role_permission")
@Schema(description = "角色权限关联实体")
public class RolePermission extends BaseEntity {

  private static final long serialVersionUID = 1L;

  /** 角色ID (不能为空) */
  @TableField("role_id")
  @Schema(description = "角色ID", example = "1", required = true)
  private Long roleId;

  /** 权限ID (不能为空) */
  @TableField("permission_id")
  @Schema(description = "权限ID", example = "1", required = true)
  private Long permissionId;
}
