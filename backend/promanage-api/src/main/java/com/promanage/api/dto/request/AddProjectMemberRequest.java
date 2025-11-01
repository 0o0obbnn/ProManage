package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加项目成员请求DTO
 *
 * <p>用于向项目中添加新成员并分配角色
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "添加项目成员请求")
public class AddProjectMemberRequest {

  /**
   * 项目ID
   *
   * <p>必填项，要添加成员的项目ID
   */
  @NotNull(message = "项目ID不能为空")
  @Schema(description = "项目ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long projectId;

  /**
   * 用户ID
   *
   * <p>必填项，要添加的用户ID
   */
  @NotNull(message = "用户ID不能为空")
  @Schema(description = "用户ID", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long userId;

  /**
   * 项目角色ID
   *
   * <p>必填项，为该成员分配的项目角色ID 常见角色：项目经理、开发人员、测试人员、文档管理员等
   */
  @NotNull(message = "角色ID不能为空")
  @Schema(description = "项目角色ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long roleId;

  /**
   * 成员备注
   *
   * <p>可选项，对该成员在项目中的角色或职责的额外说明
   */
  @Schema(description = "成员备注", example = "负责后端开发工作")
  private String remark;
}
