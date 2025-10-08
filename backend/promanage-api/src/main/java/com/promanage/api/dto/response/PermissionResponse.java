package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 权限响应DTO
 * <p>
 * 返回权限的基本信息
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限响应")
public class PermissionResponse {

    @Schema(description = "权限ID", example = "1")
    private Long id;

    @Schema(description = "权限名称", example = "创建文档")
    private String permissionName;

    @Schema(description = "权限编码", example = "document:create")
    private String permissionCode;

    @Schema(description = "权限类型 (menu/button/api)", example = "api")
    private String type;

    @Schema(description = "API路径", example = "/api/documents")
    private String url;

    @Schema(description = "前端路由路径", example = "/system/user")
    private String path;

    @Schema(description = "前端组件名称", example = "system/UserManagement")
    private String component;

    @Schema(description = "HTTP方法", example = "POST")
    private String method;

    @Schema(description = "父级权限ID", example = "0")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "图标", example = "icon-document")
    private String icon;

    @Schema(description = "权限状态 (0-正常, 1-禁用)", example = "0")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-10-08T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-10-08T15:30:00")
    private LocalDateTime updateTime;
}