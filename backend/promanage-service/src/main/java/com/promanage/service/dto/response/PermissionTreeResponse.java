package com.promanage.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限树形结构响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "权限树形结构响应")
public class PermissionTreeResponse {

    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "权限名称", example = "查看用户")
    private String permissionName;

    @Schema(description = "权限编码", example = "user:view")
    private String permissionCode;

    @Schema(description = "权限类型 (menu/button/api)", example = "api")
    private String type;

    @Schema(description = "API路径", example = "/api/users")
    private String url;

    @Schema(description = "前端路由路径", example = "/system/user")
    private String path;

    @Schema(description = "前端组件名称", example = "system/UserManagement")
    private String component;

    @Schema(description = "HTTP方法", example = "GET")
    private String method;

    @Schema(description = "父级权限ID", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "图标", example = "icon-user")
    private String icon;

    @Schema(description = "权限状态 (0-正常, 1-禁用)", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "子权限列表")
    private List<PermissionTreeResponse> children;
}
