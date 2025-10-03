package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 权限实体类
 * <p>
 * 存储系统权限信息,支持菜单权限、按钮权限和API权限
 * 支持树形结构,通过parentId构建权限层级关系
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_permission")
@Schema(description = "权限实体")
public class Permission extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 权限名称 (不能为空)
     */
    @TableField("permission_name")
    @Schema(description = "权限名称", example = "创建文档", required = true)
    private String permissionName;

    /**
     * 权限编码 (唯一, 不能为空)
     * <p>
     * 格式: 模块:操作
     * 例如: document:create, project:view, user:delete
     * </p>
     */
    @TableField("permission_code")
    @Schema(description = "权限编码", example = "document:create", required = true)
    private String permissionCode;

    /**
     * 权限类型
     * <ul>
     *   <li>menu - 菜单权限</li>
     *   <li>button - 按钮权限</li>
     *   <li>api - API权限</li>
     * </ul>
     */
    @TableField("type")
    @Schema(description = "权限类型 (menu/button/api)", example = "api")
    private String type;

    /**
     * API路径 (当type为api时使用)
     */
    @TableField("url")
    @Schema(description = "API路径", example = "/api/documents")
    private String url;

    /**
     * 前端路由路径 (当type为menu时使用)
     */
    @TableField("path")
    @Schema(description = "前端路由路径", example = "/system/user")
    private String path;

    /**
     * 前端组件名称 (当type为menu时使用)
     */
    @TableField("component")
    @Schema(description = "前端组件名称", example = "system/UserManagement")
    private String component;

    /**
     * HTTP方法 (当type为api时使用)
     * <p>
     * 例如: GET, POST, PUT, DELETE
     * </p>
     */
    @TableField("method")
    @Schema(description = "HTTP方法", example = "POST")
    private String method;

    /**
     * 父级权限ID
     * <p>
     * 0表示顶级权限
     * </p>
     */
    @TableField("parent_id")
    @Schema(description = "父级权限ID", example = "0", defaultValue = "0")
    private Long parentId;

    /**
     * 排序 (数值越小越靠前)
     */
    @TableField("sort")
    @Schema(description = "排序", example = "1", defaultValue = "0")
    private Integer sort;

    /**
     * 图标 (前端展示用)
     */
    @TableField("icon")
    @Schema(description = "图标", example = "icon-document")
    private String icon;

    /**
     * 权限状态
     * <ul>
     *   <li>0 - 正常</li>
     *   <li>1 - 禁用</li>
     * </ul>
     */
    @TableField("status")
    @Schema(description = "权限状态 (0-正常, 1-禁用)", example = "0", defaultValue = "0")
    private Integer status;

    /**
     * 子权限列表 (用于构建树形结构,不对应数据库字段)
     */
    @TableField(exist = false)
    @Schema(description = "子权限列表")
    private List<Permission> children;

    // Compatibility methods for legacy code
    public String getPermissionType() {
        return this.type;
    }

    public void setPermissionType(String permissionType) {
        this.type = permissionType;
    }

    public String getPath() {
        return this.path != null ? this.path : this.url;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component) {
        this.component = component;
    }
}