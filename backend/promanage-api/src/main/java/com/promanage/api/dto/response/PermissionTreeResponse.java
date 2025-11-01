package com.promanage.api.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 权限树形响应DTO
 *
 * <p>返回权限的树形结构，包含子权限列表
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限树形响应")
public class PermissionTreeResponse extends PermissionResponse {

  @Schema(description = "子权限列表")
  private List<PermissionTreeResponse> children;

  /** 是否有子权限 */
  @Schema(description = "是否有子权限", example = "true")
  private Boolean hasChildren;

  /** 子权限数量 */
  @Schema(description = "子权限数量", example = "3")
  private Integer childrenCount;
}
