package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限响应DTO
 * <p>
 * 返回权限信息，支持树形结构
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
@Schema(description = "权限响应")
public class PermissionResponse {

    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "权限名称", example = "用户管理")
    private String permissionName;

    @Schema(description = "权限编码", example = "user:view")
    private String permissionCode;

    @Schema(description = "权限类型：MENU-菜单，BUTTON-按钮，API-接口", example = "MENU")
    private String permissionType;

    @Schema(description = "权限类型：MENU-菜单，BUTTON-按钮，API-接口", example = "MENU")
    private String type;

    @Schema(description = "父权限ID（0表示顶级权限）", example = "0")
    private Long parentId;

    @Schema(description = "权限路径", example = "/system/user")
    private String path;

    @Schema(description = "前端组件名称", example = "system/UserManagement")
    private String component;

    @Schema(description = "权限图标", example = "user")
    private String icon;

    @Schema(description = "排序号", example = "1")
    private Integer sort;

    @Schema(description = "权限状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "权限描述", example = "查看用户列表和详情")
    private String description;

    @Schema(description = "创建时间", example = "2025-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-01T00:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "子权限列表")
    private List<PermissionResponse> children;
}