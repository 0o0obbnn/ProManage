package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新权限请求DTO
 * <p>
 * 用于更新权限信息，所有字段都是可选的
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "更新权限请求")
public class UpdatePermissionRequest {

    /**
     * 权限名称
     * <p>
     * 可选项，长度限制为1-100个字符
     * </p>
     */
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Schema(description = "权限名称", example = "更新后的权限名称")
    private String permissionName;

    /**
     * 权限编码
     * <p>
     * 可选项，长度限制为1-100个字符
     * 格式: 模块:操作，例如: document:create, project:view, user:delete
     * </p>
     */
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    @Pattern(regexp = "^[a-z]+:[a-z_]+$", message = "权限编码格式不正确，应为小写字母:小写字母或下划线")
    @Schema(description = "权限编码", example = "document:update")
    private String permissionCode;

    /**
     * 权限类型
     * <p>
     * 可选项，menu-菜单权限，button-按钮权限，api-API权限
     * </p>
     */
    @Pattern(regexp = "^(menu|button|api)$", message = "权限类型必须是menu、button或api")
    @Schema(description = "权限类型 (menu/button/api)", example = "api")
    private String type;

    /**
     * API路径
     * <p>
     * 可选项，当type为api时使用，API请求路径
     * </p>
     */
    @Schema(description = "API路径", example = "/api/documents")
    private String url;

    /**
     * 前端路由路径
     * <p>
     * 可选项，当type为menu时使用，前端路由路径
     * </p>
     */
    @Schema(description = "前端路由路径", example = "/system/user")
    private String path;

    /**
     * 前端组件名称
     * <p>
     * 可选项，当type为menu时使用，前端组件名称
     * </p>
     */
    @Schema(description = "前端组件名称", example = "system/UserManagement")
    private String component;

    /**
     * HTTP方法
     * <p>
     * 可选项，当type为api时使用，HTTP请求方法
     * </p>
     */
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "HTTP方法必须是GET、POST、PUT、DELETE或PATCH")
    @Schema(description = "HTTP方法", example = "PUT")
    private String method;

    /**
     * 父级权限ID
     * <p>
     * 可选项，0表示顶级权限
     * </p>
     */
    @Schema(description = "父级权限ID", example = "1")
    private Long parentId;

    /**
     * 排序
     * <p>
     * 可选项，数值越小越靠前
     * </p>
     */
    @Schema(description = "排序", example = "2")
    private Integer sort;

    /**
     * 图标
     * <p>
     * 可选项，前端展示用的图标
     * </p>
     */
    @Size(max = 50, message = "图标长度不能超过50个字符")
    @Schema(description = "图标", example = "icon-document-updated")
    private String icon;

    /**
     * 权限状态
     * <p>
     * 可选项，0-正常，1-禁用
     * </p>
     */
    @Schema(description = "权限状态 (0-正常, 1-禁用)", example = "0")
    private Integer status;
}