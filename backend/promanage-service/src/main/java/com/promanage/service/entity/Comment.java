package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体类
 * <p>
 * 通用评论实体，支持文档、任务、变更请求等多种实体类型的评论
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_comment")
@Schema(description = "评论信息")
public class Comment extends BaseEntity {

    /**
     * 实体类型：DOCUMENT-文档, TASK-任务, CHANGE_REQUEST-变更请求, TEST_CASE-测试用例
     */
    @Schema(description = "实体类型", example = "CHANGE_REQUEST")
    private String entityType;

    /**
     * 实体ID
     */
    @Schema(description = "实体ID", example = "1")
    private Long entityId;

    /**
     * 父评论ID（用于回复评论）
     */
    @Schema(description = "父评论ID", example = "")
    private Long parentCommentId;

    /**
     * 评论内容
     */
    @Schema(description = "评论内容", example = "这个变更请求很有必要，建议尽快实施")
    private String content;

    /**
     * 内容类型：text/plain, text/markdown, text/html
     */
    @Schema(description = "内容类型", example = "text/plain")
    private String contentType;

    /**
     * 是否为内部评论（仅项目成员可见）
     */
    @Schema(description = "是否为内部评论", example = "false")
    private Boolean isInternal;

    /**
     * 评论作者ID
     */
    @Schema(description = "评论作者ID", example = "1")
    private Long authorId;
}

