package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * @since 2025-10-08
 */
@Data
@Schema(description = "分配权限请求")
public class AssignPermissionsRequest {

    /**
     * 角色ID
     * <p>
     * 必填项，要分配权限的角色ID
     * </p>
     */
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    /**
     * 权限ID列表
     * <p>
     * 必填项，要分配给角色的权限ID列表
     * </p>
     */
    @NotNull(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> permissionIds;
}