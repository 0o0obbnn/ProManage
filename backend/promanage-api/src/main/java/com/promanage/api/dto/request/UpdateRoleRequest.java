package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新角色请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "更新角色请求")
public class UpdateRoleRequest {

  @Size(max = 50, message = "角色名称长度不能超过50个字符")
  @Schema(description = "角色名称", example = "高级项目经理")
  private String roleName;

  @Size(max = 50, message = "角色编码长度不能超过50个字符")
  @Schema(description = "角色编码", example = "SENIOR_PROJECT_MANAGER")
  private String roleCode;

  @Size(max = 200, message = "角色描述长度不能超过200个字符")
  @Schema(description = "角色描述", example = "负责多个项目的整体规划和管理")
  private String description;

  @Schema(description = "排序号", example = "2")
  private Integer sort;

  @Schema(description = "角色状态：0-禁用，1-启用", example = "1")
  private Integer status;
}
