package com.promanage.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限路由信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限路由信息")
public class PermissionRouteInfo {

  @Schema(description = "API路径", example = "/api/users")
  private String url;

  @Schema(description = "前端路由路径", example = "/system/user")
  private String path;

  @Schema(description = "前端组件名称", example = "system/UserManagement")
  private String component;

  @Schema(description = "HTTP方法", example = "GET")
  private String method;
}
