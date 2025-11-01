package com.promanage.api.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建文档请求DTO
 *
 * <p>用于创建新文档，支持多种文档类型
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "创建文档请求")
public class CreateDocumentRequest {

  /**
   * 文档标题
   *
   * <p>必填项，长度限制为1-200个字符
   */
  @NotBlank(message = "文档标题不能为空")
  @Size(max = 200, message = "文档标题长度不能超过200个字符")
  @Schema(description = "文档标题", example = "用户管理模块需求文档", requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  /**
   * 文档内容
   *
   * <p>必填项，支持Markdown格式或富文本
   */
  @NotBlank(message = "文档内容不能为空")
  @Schema(
      description = "文档内容（支持Markdown）",
      example = "# 用户管理模块\n\n## 功能概述\n...",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String content;

  /**
   * 内容类型
   *
   * <p>可选项，默认为markdown
   */
  @Schema(
      description = "内容类型：markdown, html, rich_text",
      example = "markdown",
      defaultValue = "markdown")
  private String contentType = "markdown";

  /**
   * 文档摘要
   *
   * <p>可选项，最大长度500个字符，用于快速预览
   */
  @Size(max = 500, message = "文档摘要长度不能超过500个字符")
  @Schema(description = "文档摘要", example = "本文档描述了用户管理模块的功能需求和设计方案")
  private String summary;

  /**
   * 文档类型
   *
   * <p>必填项，支持：PRD-需求文档，Design-设计文档，API-接口文档，Test-测试文档，Other-其他
   */
  @NotBlank(message = "文档类型不能为空")
  @Schema(
      description = "文档类型：PRD, Design, API, Test, Other",
      example = "PRD",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String type;

  /**
   * 分类ID
   *
   * <p>可选项，文档分类ID
   */
  @Schema(description = "分类ID", example = "1")
  private Long categoryId;

  /**
   * 标签列表
   *
   * <p>可选项，文档标签
   */
  @Schema(description = "标签列表", example = "[\"需求\", \"后端\", \"用户模块\"]")
  private List<String> tags;

  /**
   * 是否为模板
   *
   * <p>可选项，默认为false
   */
  @Schema(description = "是否为模板", example = "false", defaultValue = "false")
  private Boolean isTemplate = false;

  /**
   * 所属项目ID
   *
   * <p>必填项，文档必须关联到某个项目
   */
  @NotNull(message = "项目ID不能为空")
  @Schema(description = "所属项目ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long projectId;

  /**
   * 文件夹ID
   *
   * <p>可选项，文档所在的文件夹ID，0表示根目录
   */
  @Schema(description = "文件夹ID（0表示根目录）", example = "0", defaultValue = "0")
  private Long folderId = 0L;

  /**
   * 文档优先级
   *
   * <p>可选项，1-低，2-中（默认），3-高
   */
  @Schema(description = "文档优先级：1-低，2-中，3-高", example = "2")
  private Integer priority;
}
