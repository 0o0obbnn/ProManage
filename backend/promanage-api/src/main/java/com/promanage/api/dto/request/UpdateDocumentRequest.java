package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新文档请求DTO
 * <p>
 * 用于更新文档内容和属性，所有字段都是可选的
 * 每次更新会自动生成新版本
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "更新文档请求")
public class UpdateDocumentRequest {

    /**
     * 文档标题
     * <p>
     * 可选项，长度限制为1-200个字符
     * </p>
     */
    @Size(max = 200, message = "文档标题长度不能超过200个字符")
    @Schema(description = "文档标题", example = "用户管理模块需求文档v2.0")
    private String title;

    /**
     * 文档内容
     * <p>
     * 可选项，支持Markdown格式或富文本
     * </p>
     */
    @Schema(description = "文档内容（支持Markdown）", example = "# 更新后的内容...")
    private String content;

    /**
     * 内容类型
     * <p>
     * 可选项
     * </p>
     */
    @Schema(description = "内容类型：markdown, html, rich_text", example = "markdown")
    private String contentType;

    /**
     * 文档摘要
     * <p>
     * 可选项，最大长度500个字符
     * </p>
     */
    @Size(max = 500, message = "文档摘要长度不能超过500个字符")
    @Schema(description = "文档摘要", example = "更新了需求描述和验收标准")
    private String summary;

    /**
     * 文档类型
     * <p>
     * 可选项，支持：PRD-需求文档，Design-设计文档，API-接口文档，Test-测试文档，Other-其他
     * </p>
     */
    @Schema(description = "文档类型：PRD, Design, API, Test, Other", example = "PRD")
    private String type;

    /**
     * 分类ID
     * <p>
     * 可选项，文档分类ID
     * </p>
     */
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    /**
     * 标签列表
     * <p>
     * 可选项，文档标签
     * </p>
     */
    @Schema(description = "标签列表", example = "[\"需求\", \"后端\", \"用户模块\", \"v2.0\"]")
    private List<String> tags;

    /**
     * 文档状态
     * <p>
     * 可选项，文档状态
     * </p>
     */
    @Schema(description = "文档状态：DRAFT, UNDER_REVIEW, APPROVED, ARCHIVED, DEPRECATED", example = "UNDER_REVIEW")
    private String status;

    /**
     * 审核人ID
     * <p>
     * 可选项，指定审核人
     * </p>
     */
    @Schema(description = "审核人ID", example = "5")
    private Long reviewerId;

    /**
     * 变更说明
     * <p>
     * 可选项，描述本次变更的内容，用于版本历史记录
     * </p>
     */
    @Size(max = 500, message = "变更说明长度不能超过500个字符")
    @Schema(description = "变更说明", example = "添加了新的功能需求，修改了部分业务逻辑")
    private String changelog;

    /**
     * 文件夹ID
     * <p>
     * 可选项，移动文档到其他文件夹
     * </p>
     */
    @Schema(description = "文件夹ID", example = "5")
    private Long folderId;

    /**
     * 文档优先级
     * <p>
     * 可选项，1-低，2-中，3-高
     * </p>
     */
    @Schema(description = "文档优先级：1-低，2-中，3-高", example = "3")
    private Integer priority;
}