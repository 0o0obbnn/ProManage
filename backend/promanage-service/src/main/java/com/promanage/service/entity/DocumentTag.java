package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档标签关联实体类
 * <p>
 * 文档和标签的多对多关联关系
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@TableName("document_tags")
@Schema(description = "文档标签关联")
public class DocumentTag {

    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID", example = "1")
    private Long id;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    /**
     * 标签ID
     */
    @Schema(description = "标签ID", example = "1")
    private Long tagId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long creatorId;
}