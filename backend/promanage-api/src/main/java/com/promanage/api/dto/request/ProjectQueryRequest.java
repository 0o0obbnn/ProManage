package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 项目查询请求DTO
 *
 * <p>用于分页查询和搜索项目列表，支持多条件组合查询
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "项目查询请求")
public class ProjectQueryRequest {

  /**
   * 搜索关键词
   *
   * <p>可选项，支持模糊匹配项目名称、项目编码和描述
   */
  @Schema(description = "搜索关键词（匹配项目名称、编码、描述）", example = "ProManage")
  private String keyword;

  /**
   * 项目状态
   *
   * <p>可选项，0-未开始，1-进行中，2-已完成，3-已暂停，4-已取消
   */
  @Schema(description = "项目状态：0-未开始，1-进行中，2-已完成，3-已暂停，4-已取消", example = "1")
  private Integer status;

  /**
   * 项目负责人ID
   *
   * <p>可选项，查询指定负责人的项目
   */
  @Schema(description = "项目负责人ID", example = "1")
  private Long ownerId;

  /**
   * 项目成员ID
   *
   * <p>可选项，查询指定成员参与的项目
   */
  @Schema(description = "项目成员ID", example = "2")
  private Long memberId;

  /**
   * 项目类型
   *
   * <p>可选项，项目类型：WEB, APP, SYSTEM, OTHER
   */
  @Schema(description = "项目类型：WEB, APP, SYSTEM, OTHER", example = "WEB")
  private String type;

  /**
   * 项目优先级
   *
   * <p>可选项，1-低，2-中，3-高，4-紧急
   */
  @Schema(description = "项目优先级：1-低，2-中，3-高，4-紧急", example = "3")
  private Integer priority;

  /**
   * 创建时间-开始
   *
   * <p>可选项，查询在此时间之后创建的项目
   */
  @Schema(description = "创建时间-开始", example = "2025-01-01")
  private String createTimeStart;

  /**
   * 创建时间-结束
   *
   * <p>可选项，查询在此时间之前创建的项目
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
   * <p>可选项，支持：createTime, updateTime, startDate, name
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
