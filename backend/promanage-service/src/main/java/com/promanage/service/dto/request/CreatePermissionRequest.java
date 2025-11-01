package com.promanage.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建权限请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "创建权限请求")
public class CreatePermissionRequest {

  @NotBlank(message = "权限名称不能为空")
  @Schema(description = "权限名称", example = "查看用户", required = true)
  private String permissionName;

  @NotBlank(message = "权限编码不能为空")
  @Schema(description = "权限编码", example = "user:view", required = true)
  private String permissionCode;

  @Schema(description = "权限类型 (menu/button/api)", example = "api")
  private String type;

  @Schema(description = "API路径", example = "/api/users")
  private String url;

  @Schema(description = "前端路由路径", example = "/system/user")
  private String path;

  @Schema(description = "前端组件名称", example = "system/UserManagement")
  private String component;

  @Schema(description = "HTTP方法", example = "GET")
  private String method;

  @Schema(description = "父级权限ID", example = "0")
  private Long parentId;

  @Schema(description = "排序", example = "1")
  private Integer sort;

  @Schema(description = "图标", example = "icon-user")
  private String icon;

  @Schema(description = "权限状态 (0-正常, 1-禁用)", example = "0")
  private Integer status;

  @Schema(description = "组织ID (不填或为NULL表示创建系统级权限)", example = "1")
  private Long organizationId;
}
