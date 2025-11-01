package com.promanage.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限树形结构响应DTO - 使用组合模式减少字段数量
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限树形结构响应")
public class PermissionTreeResponse {

  @Schema(description = "权限基本信息")
  private PermissionBasicInfo basicInfo;

  @Schema(description = "权限路由信息")
  private PermissionRouteInfo routeInfo;

  @Schema(description = "权限树形信息")
  private PermissionTreeInfo treeInfo;

  @Schema(description = "子权限列表")
  private java.util.List<PermissionTreeResponse> children;
}
