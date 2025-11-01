package com.promanage.api.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户详情响应DTO
 *
 * <p>扩展用户响应，包含权限和项目信息
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户详情响应")
public class UserDetailResponse extends UserResponse {

  @Schema(description = "用户权限列表")
  private List<PermissionResponse> permissions;

  @Schema(description = "用户角色ID列表")
  private List<Long> roleIds;

  @Schema(description = "用户参与的项目列表")
  private List<ProjectResponse> projects;

  @Schema(description = "创建的文档数量", example = "25")
  private Integer documentCount;

  @Schema(description = "参与的项目数量", example = "5")
  private Integer projectCount;
}
