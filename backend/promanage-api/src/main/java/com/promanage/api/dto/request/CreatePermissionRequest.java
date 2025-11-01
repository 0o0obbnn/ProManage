package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建权限请求DTO
 *
 * <p>用于创建新的权限，包含权限的基本信息和配置
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "创建权限请求")
public class CreatePermissionRequest {

  /**
   * 权限名称
   *
   * <p>必填项，长度限制为1-100个字符
   */
  @NotBlank(message = "权限名称不能为空")
  @Size(max = 100, message = "权限名称长度不能超过100个字符")
  @Schema(description = "权限名称", example = "创建文档", requiredMode = Schema.RequiredMode.REQUIRED)
  private String permissionName;

  /**
   * 权限编码
   *
   * <p>必填项，长度限制为1-100个字符 格式: 模块:操作，例如: document:create, project:view, user:delete
   */
  @NotBlank(message = "权限编码不能为空")
  @Size(max = 100, message = "权限编码长度不能超过100个字符")
  @Pattern(regexp = "^[a-z]+:[a-z_]+$", message = "权限编码格式不正确，应为小写字母:小写字母或下划线")
  @Schema(
      description = "权限编码",
      example = "document:create",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String permissionCode;

  /**
   * 权限类型
   *
   * <p>必填项，menu-菜单权限，button-按钮权限，api-API权限
   */
  @NotBlank(message = "权限类型不能为空")
  @Pattern(regexp = "^(menu|button|api)$", message = "权限类型必须是menu、button或api")
  @Schema(
      description = "权限类型 (menu/button/api)",
      example = "api",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String type;

  /**
   * API路径
   *
   * <p>当type为api时必填，API请求路径
   */
  @Schema(description = "API路径", example = "/api/documents")
  private String url;

  /**
   * 前端路由路径
   *
   * <p>当type为menu时使用，前端路由路径
   */
  @Schema(description = "前端路由路径", example = "/system/user")
  private String path;

  /**
   * 前端组件名称
   *
   * <p>当type为menu时使用，前端组件名称
   */
  @Schema(description = "前端组件名称", example = "system/UserManagement")
  private String component;

  /**
   * HTTP方法
   *
   * <p>当type为api时使用，HTTP请求方法
   */
  @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH)$", message = "HTTP方法必须是GET、POST、PUT、DELETE或PATCH")
  @Schema(description = "HTTP方法", example = "POST")
  private String method;

  /**
   * 父级权限ID
   *
   * <p>可选项，0表示顶级权限
   */
  @Schema(description = "父级权限ID", example = "0")
  private Long parentId;

  /**
   * 排序
   *
   * <p>可选项，数值越小越靠前
   */
  @Schema(description = "排序", example = "1")
  private Integer sort;

  /**
   * 图标
   *
   * <p>可选项，前端展示用的图标
   */
  @Size(max = 50, message = "图标长度不能超过50个字符")
  @Schema(description = "图标", example = "icon-document")
  private String icon;
}
