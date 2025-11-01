package com.promanage.service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限树形信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限树形信息")
public class PermissionTreeInfo {

  @Schema(description = "父级权限ID", example = "0")
  private Long parentId;

  @Schema(description = "排序", example = "1")
  private Integer sort;

  @Schema(description = "图标", example = "icon-user")
  private String icon;

  @Schema(description = "创建时间")
  private LocalDateTime createTime;

  @Schema(description = "更新时间")
  private LocalDateTime updateTime;

  @Schema(description = "子权限列表")
  private List<PermissionTreeResponse> children;
}
