package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建角色请求DTO
 *
 * <p>用于创建新的系统角色或项目角色
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "创建角色请求")
public class CreateRoleRequest {

  @NotBlank(message = "角色名称不能为空")
  @Size(max = 50, message = "角色名称长度不能超过50个字符")
  @Schema(description = "角色名称", example = "项目经理", requiredMode = Schema.RequiredMode.REQUIRED)
  private String roleName;

  @NotBlank(message = "角色编码不能为空")
  @Size(max = 50, message = "角色编码长度不能超过50个字符")
  @Schema(
      description = "角色编码",
      example = "PROJECT_MANAGER",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String roleCode;

  @Size(max = 200, message = "角色描述长度不能超过200个字符")
  @Schema(description = "角色描述", example = "负责项目的整体规划和管理")
  private String description;

  @Schema(description = "排序号", example = "1")
  private Integer sort;

  @Schema(description = "角色状态：0-禁用，1-启用", example = "1")
  private Integer status;

  @Schema(description = "权限ID列表")
  private java.util.List<Long> permissionIds;
}
