package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色响应DTO
 * <p>
 * 返回角色信息和关联的权限
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色响应")
public class RoleResponse {

    @Schema(description = "角色ID", example = "1")
    private Long id;

    @Schema(description = "角色名称", example = "项目经理")
    private String roleName;

    @Schema(description = "角色编码", example = "PROJECT_MANAGER")
    private String roleCode;

    @Schema(description = "角色描述", example = "负责项目的整体规划和管理")
    private String description;

    @Schema(description = "排序号", example = "1")
    private Integer sort;

    @Schema(description = "角色状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "角色类型：SYSTEM-系统角色，PROJECT-项目角色", example = "PROJECT")
    private String type;

    @Schema(description = "创建时间", example = "2025-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-09-30T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "角色拥有的权限列表")
    private List<PermissionResponse> permissions;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    @Schema(description = "用户数量", example = "15")
    private Integer userCount;
}