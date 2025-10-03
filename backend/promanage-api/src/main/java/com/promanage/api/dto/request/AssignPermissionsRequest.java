package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配权限请求DTO
 * <p>
 * 用于为角色分配权限
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "分配权限请求")
public class AssignPermissionsRequest {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", example = "[1, 2, 3, 5, 8]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> permissionIds;
}