package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档版本实体类
 * <p>
 * 存储文档的历史版本信息,支持版本回溯和变更追踪
 * 每次文档更新时自动创建新版本记录
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_document_version")
@Schema(description = "文档版本实体")
public class DocumentVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文档ID (不能为空)
     */
    @TableField("document_id")
    @Schema(description = "文档ID", example = "1", required = true)
    private Long documentId;

    /**
     * 版本号 (不能为空)
     * <p>
     * 建议使用语义化版本号
     * 格式: 主版本号.次版本号.修订号
     * 例如: 1.0.0, 1.0.1, 2.0.0
     * </p>
     */
    @TableField("version_number")
    @Schema(description = "版本号", example = "1.0.0", required = true)
    private String versionNumber;

    /**
     * 版本标题
     */
    @TableField("title")
    @Schema(description = "版本标题", example = "用户管理模块需求文档")
    private String title;

    /**
     * 版本内容
     * <p>
     * 该版本的完整文档内容
     * </p>
     */
    @TableField("content")
    @Schema(description = "版本内容")
    private String content;

    /**
     * 内容类型
     * <p>
     * 支持markdown, html, rich_text
     * </p>
     */
    @TableField("content_type")
    @Schema(description = "内容类型 (markdown/html/rich_text)", example = "markdown", defaultValue = "markdown")
    private String contentType = "markdown";

    /**
     * 变更日志
     * <p>
     * 描述本次版本的主要变更内容
     * </p>
     */
    @TableField("change_log")
    @Schema(description = "变更日志", example = "新增功能模块说明,修正错别字")
    private String changeLog;

    /**
     * 文件大小 (字节)
     */
    @TableField("file_size")
    @Schema(description = "文件大小(字节)", example = "1048576")
    private Long fileSize;

    /**
     * 版本文件URL
     * <p>
     * 存储该版本文档文件的访问路径
     * </p>
     */
    @TableField("file_url")
    @Schema(description = "版本文件URL", example = "https://cdn.example.com/files/doc123_v1.0.0.pdf")
    private String fileUrl;

    /**
     * 内容哈希值
     * <p>
     * 用于版本内容去重和一致性校验
     * </p>
     */
    @TableField("content_hash")
    @Schema(description = "内容哈希值", example = "a1b2c3d4e5f6...")
    private String contentHash;

    /**
     * 是否为当前版本
     */
    @TableField("is_current")
    @Schema(description = "是否为当前版本", example = "true", defaultValue = "false")
    private Boolean isCurrent = false;

    /**
     * 创建人ID (不能为空)
     * <p>
     * 该版本的创建者
     * </p>
     */
    @TableField("creator_id")
    @Schema(description = "创建人ID", example = "1", required = true)
    private Long creatorId;
}