package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 任务检查项响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务检查项响应")
public class TaskCheckItemResponse {

  @Schema(description = "检查项ID", example = "1")
  private Long id;

  @Schema(description = "检查项内容", example = "完成前端界面设计")
  private String content;

  @Schema(description = "是否完成", example = "false")
  private Boolean isCompleted;

  @Schema(description = "完成者ID", example = "1")
  private Long completedById;

  @Schema(description = "完成时间", example = "2025-10-02T14:30:00")
  private LocalDateTime completedTime;

  @Schema(description = "排序序号", example = "1")
  private Integer sortOrder;

  @Schema(description = "创建时间", example = "2025-10-02T10:30:00")
  private LocalDateTime createTime;
}
