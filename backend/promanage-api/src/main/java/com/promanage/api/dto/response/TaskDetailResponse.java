package com.promanage.api.dto.response;

import com.promanage.api.dto.response.TaskActivityResponse;
import com.promanage.api.dto.response.TaskAttachmentResponse;
import com.promanage.api.dto.response.TaskCheckItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 任务详情响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Schema(description = "任务详情响应")
public class TaskDetailResponse extends TaskResponse {

    @Schema(description = "子任务列表")
    private List<TaskResponse> subtasks;

    @Schema(description = "依赖任务列表")
    private List<TaskResponse> dependencies;

    @Schema(description = "最近的评论列表")
    private List<TaskCommentResponse> recentComments;

    @Schema(description = "最近的动态列表")
    private List<TaskActivityResponse> recentActivities;

    @Schema(description = "任务描述（完整HTML格式）")
    private String fullDescription;

    @Schema(description = "任务附件列表")
    private List<TaskAttachmentResponse> attachments;

    @Schema(description = "任务检查项列表")
    private List<TaskCheckItemResponse> checkItems;
}

/**
 * 任务检查项
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "任务检查项")
class TaskCheckItem {
    @Schema(description = "检查项ID", example = "1")
    private Long id;

    @Schema(description = "检查项内容", example = "完成用户界面设计")
    private String content;

    @Schema(description = "是否已完成", example = "true")
    private Boolean completed;

    @Schema(description = "排序", example = "1")
    private Integer sort;
}