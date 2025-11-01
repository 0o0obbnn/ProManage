package com.promanage.service.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分配权限请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "分配权限请求")
public class AssignPermissionsRequest {

  @NotNull(message = "角色ID不能为空")
  @Schema(description = "角色ID", example = "1", required = true)
  private Long roleId;

  @Schema(description = "权限ID列表", example = "[1, 2, 3]")
  private List<Long> permissionIds;
}
