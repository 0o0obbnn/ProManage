package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新任务请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@Schema(description = "批量更新任务请求")
public class BatchUpdateTasksRequest {

    /**
     * 任务ID列表
     */
    @NotEmpty(message = "任务ID列表不能为空")
    @Schema(description = "任务ID列表", required = true, example = "[1, 2, 3]")
    private List<Long> taskIds;

    /**
     * 新状态（可选）
     */
    @Schema(description = "新状态", example = "1")
    private Integer status;

    /**
     * 新优先级（可选）
     */
    @Schema(description = "新优先级", example = "2")
    private Integer priority;

    /**
     * 新指派人ID（可选）
     */
    @Schema(description = "新指派人ID", example = "5")
    private Long assigneeId;

    /**
     * 标签（可选）
     */
    @Schema(description = "标签", example = "bug,urgent")
    private String tags;
}
