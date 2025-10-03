package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档实体类
 * <p>
 * 存储项目文档的基本信息、内容和版本管理
 * 支持多种文档类型和状态管理
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_document")
@Schema(description = "文档实体")
public class Document extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文档标题 (不能为空)
     */
    @TableField("title")
    @Schema(description = "文档标题", example = "ProManage产品需求文档", required = true)
    private String title;

    /**
     * 文档内容
     * <p>
     * 支持富文本或Markdown格式
     * </p>
     */
    @TableField("content")
    @Schema(description = "文档内容")
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
     * 文档摘要
     * <p>
     * 用于搜索和预览
     * </p>
     */
    @TableField("summary")
    @Schema(description = "文档摘要", example = "本文档描述了ProManage系统的核心功能和业务流程")
    private String summary;

    /**
     * 文档类型 (不能为空)
     * <ul>
     *   <li>PRD - 产品需求文档</li>
     *   <li>Design - 设计文档</li>
     *   <li>API - API文档</li>
     *   <li>Test - 测试文档</li>
     *   <li>Other - 其他文档</li>
     * </ul>
     */
    @TableField("type")
    @Schema(description = "文档类型 (PRD/Design/API/Test/Other)", example = "PRD", required = true)
    private String type;

    /**
     * 文档状态
     * <ul>
     *   <li>0 - 草稿</li>
     *   <li>1 - 审核中</li>
     *   <li>2 - 已发布</li>
     *   <li>3 - 已归档</li>
     * </ul>
     */
    @TableField("status")
    @Schema(description = "文档状态 (0-草稿, 1-审核中, 2-已发布, 3-已归档)", example = "0", defaultValue = "0")
    private Integer status;

    /**
     * 分类ID
     */
    @TableField("category_id")
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    /**
     * 所属项目ID (不能为空)
     */
    @TableField("project_id")
    @Schema(description = "所属项目ID", example = "1", required = true)
    private Long projectId;

    /**
     * 所属文件夹ID
     * <p>
     * 0表示根目录
     * </p>
     */
    @TableField("folder_id")
    @Schema(description = "所属文件夹ID", example = "0", defaultValue = "0")
    private Long folderId;

    /**
     * 附件URL
     * <p>
     * 存储文档附件的访问路径
     * </p>
     */
    @TableField("file_url")
    @Schema(description = "附件URL", example = "https://cdn.example.com/files/doc123.pdf")
    private String fileUrl;

    /**
     * 文件大小 (字节)
     */
    @TableField("file_size")
    @Schema(description = "文件大小(字节)", example = "1048576")
    private Long fileSize;

    /**
     * 当前版本号
     */
    @TableField("current_version")
    @Schema(description = "当前版本号", example = "1.0.0", defaultValue = "1.0.0")
    private String currentVersion;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    @Schema(description = "浏览次数", example = "100", defaultValue = "0")
    private Integer viewCount;

    /**
     * 是否为模板
     */
    @TableField("is_template")
    @Schema(description = "是否为模板", example = "false", defaultValue = "false")
    private Boolean isTemplate = false;

    /**
     * 优先级
     * <p>
     * 1-低，2-中，3-高
     * </p>
     */
    @TableField("priority")
    @Schema(description = "优先级：1-低，2-中，3-高", example = "2")
    private Integer priority;

    /**
     * 审核人ID
     */
    @TableField("reviewer_id")
    @Schema(description = "审核人ID", example = "5")
    private Long reviewerId;

    /**
     * 发布时间
     */
    @TableField("published_at")
    @Schema(description = "发布时间")
    private java.time.LocalDateTime publishedAt;

    /**
     * 归档时间
     */
    @TableField("archived_at")
    @Schema(description = "归档时间")
    private java.time.LocalDateTime archivedAt;

    /**
     * 创建人ID (不能为空)
     * <p>
     * 注意: BaseEntity中已有creatorId,此字段用于业务关联
     * </p>
     */
    @TableField("creator_id")
    @Schema(description = "创建人ID", example = "1", required = true)
    private Long creatorId;

    /**
     * 最后更新人ID
     */
    @TableField("updater_id")
    @Schema(description = "最后更新人ID", example = "1")
    private Long updaterId;
}