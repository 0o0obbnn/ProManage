package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


/**
 * 创建任务评论请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@Schema(description = "创建任务评论请求")
public class CreateTaskCommentRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    @Schema(description = "评论内容", example = "这个功能需要重新设计，建议采用新的交互方式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "父评论ID（用于回复评论）", example = "")
    private Long parentCommentId;

    @Schema(description = "是否为内部评论（仅项目成员可见）", example = "false")
    private Boolean isInternal = false;
}