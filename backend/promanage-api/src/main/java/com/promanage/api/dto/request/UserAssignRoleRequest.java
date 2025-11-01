package com.promanage.api.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户分配角色请求DTO
 *
 * <p>用于为用户分配角色的请求参数
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "用户分配角色请求")
public class UserAssignRoleRequest {

  @NotNull(message = "角色ID列表不能为空")
  @Schema(description = "角色ID列表", example = "[1, 2]", required = true)
  private List<Long> roleIds;
}
