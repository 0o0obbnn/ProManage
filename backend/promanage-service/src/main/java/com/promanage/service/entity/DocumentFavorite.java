package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档收藏实体类
 * <p>
 * 用户收藏文档的关系记录
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@TableName("document_favorites")
@Schema(description = "文档收藏")
public class DocumentFavorite {

    /**
     * 收藏ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "收藏ID", example = "1")
    private Long id;

    /**
     * 文档ID
     */
    @Schema(description = "文档ID", example = "1")
    private Long documentId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "收藏时间")
    private LocalDateTime createdAt;

    /**
     * 收藏文件夹ID（可选，用于分类收藏）
     */
    @Schema(description = "收藏文件夹ID", example = "1")
    private Long folderId;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "重要文档")
    private String remark;
}