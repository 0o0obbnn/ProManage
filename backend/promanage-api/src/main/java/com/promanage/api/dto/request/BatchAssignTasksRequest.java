package com.promanage.api.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批量分配任务请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@Schema(description = "批量分配任务请求")
public class BatchAssignTasksRequest {

  /** 任务ID列表 */
  @NotEmpty(message = "任务ID列表不能为空")
  @Schema(description = "任务ID列表", required = true, example = "[1, 2, 3]")
  private List<Long> taskIds;

  /** 指派人ID */
  @NotNull(message = "指派人ID不能为空")
  @Schema(description = "指派人ID", required = true, example = "5")
  private Long assigneeId;
}
