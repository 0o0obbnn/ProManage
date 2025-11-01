package com.promanage.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限基本信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限基本信息")
public class PermissionBasicInfo {

  @Schema(description = "权限ID", example = "1")
  private Long id;

  @Schema(description = "组织ID (NULL表示系统级权限)", example = "1")
  private Long organizationId;

  @Schema(description = "权限名称", example = "查看用户")
  private String permissionName;

  @Schema(description = "权限编码", example = "user:view")
  private String permissionCode;

  @Schema(description = "权限类型 (menu/button/api)", example = "api")
  private String type;

  @Schema(description = "权限状态 (0-正常, 1-禁用)", example = "0")
  private Integer status;
}
