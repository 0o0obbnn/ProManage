package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务评论实体类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_comment")
@Schema(description = "任务评论信息")
public class TaskComment extends BaseEntity {

    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "评论ID", example = "1")
    private Long id;

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容", example = "这个功能需要重新设计，建议采用新的交互方式")
    private String content;

    /**
     * 评论作者ID
     */
    @Schema(description = "评论作者ID", example = "1")
    private Long authorId;

    /**
     * 父评论ID（用于回复评论）
     */
    @Schema(description = "父评论ID（用于回复评论）", example = "")
    private Long parentCommentId;

    /**
     * 是否为内部评论（仅项目成员可见）
     */
    @Schema(description = "是否为内部评论（仅项目成员可见）", example = "false")
    private Boolean isInternal;

    /**
     * 评论状态
     * 0-正常, 1-已删除
     */
    @Schema(description = "评论状态 (0-正常, 1-已删除)", example = "0")
    private Integer status;
}