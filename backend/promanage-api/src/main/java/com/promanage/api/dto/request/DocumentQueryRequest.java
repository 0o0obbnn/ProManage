package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档查询请求DTO
 *
 * <p>用于分页查询和搜索文档列表，支持多条件组合查询
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "文档查询请求")
public class DocumentQueryRequest {

  /**
   * 搜索关键词
   *
   * <p>可选项，支持模糊匹配文档标题、摘要和标签
   */
  @Schema(description = "搜索关键词（匹配标题、摘要、标签）", example = "用户管理")
  private String keyword;

  /**
   * 文档类型
   *
   * <p>可选项，PRD, Design, API, Test, Other
   */
  @Schema(description = "文档类型：PRD, Design, API, Test, Other", example = "PRD")
  private String type;

  /**
   * 文档状态
   *
   * <p>可选项，0-草稿，1-审核中，2-已发布，3-已归档
   */
  @Schema(description = "文档状态：0-草稿，1-审核中，2-已发布，3-已归档", example = "2")
  private Integer status;

  /**
   * 所属项目ID
   *
   * <p>可选项，查询指定项目的文档
   */
  @Schema(description = "所属项目ID", example = "1")
  private Long projectId;

  /**
   * 文件夹ID
   *
   * <p>可选项，查询指定文件夹下的文档
   */
  @Schema(description = "文件夹ID", example = "3")
  private Long folderId;

  /**
   * 创建者ID
   *
   * <p>可选项，查询指定用户创建的文档
   */
  @Schema(description = "创建者ID", example = "5")
  private Long creatorId;

  /**
   * 文档标签
   *
   * <p>可选项，按标签筛选文档
   */
  @Schema(description = "文档标签", example = "需求")
  private String tag;

  /**
   * 文档优先级
   *
   * <p>可选项，1-低，2-中，3-高
   */
  @Schema(description = "文档优先级：1-低，2-中，3-高", example = "3")
  private Integer priority;

  /**
   * 创建时间-开始
   *
   * <p>可选项，查询在此时间之后创建的文档
   */
  @Schema(description = "创建时间-开始", example = "2025-01-01")
  private String createTimeStart;

  /**
   * 创建时间-结束
   *
   * <p>可选项，查询在此时间之前创建的文档
   */
  @Schema(description = "创建时间-结束", example = "2025-12-31")
  private String createTimeEnd;

  /**
   * 当前页码
   *
   * <p>可选项，默认为第1页
   */
  @Schema(description = "当前页码", example = "1", defaultValue = "1")
  private Integer page = 1;

  /**
   * 每页记录数
   *
   * <p>可选项，默认为20条，最大不超过100条
   */
  @Schema(description = "每页记录数", example = "20", defaultValue = "20")
  private Integer pageSize = 20;

  /**
   * 排序字段
   *
   * <p>可选项，支持：createTime, updateTime, viewCount, title
   */
  @Schema(description = "排序字段", example = "createTime")
  private String sortField;

  /**
   * 排序方向
   *
   * <p>可选项，asc-升序，desc-降序（默认）
   */
  @Schema(description = "排序方向：asc-升序，desc-降序", example = "desc", defaultValue = "desc")
  private String sortOrder = "desc";
}
