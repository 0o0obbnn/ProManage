package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 任务评论响应DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务评论响应")
public class TaskCommentResponse {

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "评论内容", example = "这个功能需要重新设计，建议采用新的交互方式")
    private String content;

    @Schema(description = "评论作者ID", example = "1")
    private Long authorId;

    @Schema(description = "评论作者姓名", example = "张三")
    private String authorName;

    @Schema(description = "评论作者头像", example = "https://example.com/avatar/user1.jpg")
    private String authorAvatar;

    @Schema(description = "父评论ID（用于回复）", example = "")
    private Long parentCommentId;

    @Schema(description = "是否为内部评论（仅项目成员可见）", example = "false")
    private Boolean isInternal;

    @Schema(description = "创建时间", example = "2025-10-02T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-10-02T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "回复的评论信息")
    private TaskCommentResponse parentComment;

    @Schema(description = "子回复数量", example = "3")
    private Integer replyCount;

    @Schema(description = "是否已编辑", example = "false")
    private Boolean isEdited;
}