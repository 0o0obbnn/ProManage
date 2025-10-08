package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除任务请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-05
 */
@Data
@Schema(description = "批量删除任务请求")
public class BatchDeleteTasksRequest {

    /**
     * 任务ID列表
     */
    @NotEmpty(message = "任务ID列表不能为空")
    @Schema(description = "任务ID列表", required = true, example = "[1, 2, 3]")
    private List<Long> taskIds;
}
