package com.promanage.api.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量添加项目成员请求DTO
 *
 * <p>用于批量向项目中添加多个成员并分配角色
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "批量添加项目成员请求")
public class BatchAddProjectMembersRequest {

  /**
   * 项目ID
   *
   * <p>必填项，要添加成员的项目ID
   */
  @NotNull(message = "项目ID不能为空")
  @Schema(description = "项目ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long projectId;

  /**
   * 成员列表
   *
   * <p>必填项，要添加的成员列表，每个成员包含用户ID和角色ID
   */
  @NotEmpty(message = "成员列表不能为空")
  @Schema(description = "成员列表", requiredMode = Schema.RequiredMode.REQUIRED)
  private List<MemberItem> members;

  /**
   * 成员项
   *
   * <p>包含用户ID和角色ID的成员信息
   */
  @Data
  @Schema(description = "成员项")
  public static class MemberItem {

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
     * <p>必填项，为该成员分配的项目角色ID
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
}
