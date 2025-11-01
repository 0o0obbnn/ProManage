package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求DTO
 *
 * <p>用于分页查询和搜索用户列表，支持多条件组合查询
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

  /**
   * 搜索关键词
   *
   * <p>可选项，支持模糊匹配用户名、真实姓名、邮箱和手机号
   */
  @Schema(description = "搜索关键词（匹配用户名、姓名、邮箱、手机号）", example = "张三")
  private String keyword;

  /**
   * 用户状态
   *
   * <p>可选项，0-禁用，1-正常，2-锁定
   */
  @Schema(description = "用户状态：0-禁用，1-正常，2-锁定", example = "1")
  private Integer status;

  /**
   * 角色ID
   *
   * <p>可选项，按角色筛选用户
   */
  @Schema(description = "角色ID", example = "2")
  private Long roleId;

  /**
   * 部门名称
   *
   * <p>可选项，按部门筛选用户
   */
  @Schema(description = "部门名称", example = "研发部")
  private String department;

  /**
   * 创建时间-开始
   *
   * <p>可选项，查询在此时间之后创建的用户
   */
  @Schema(description = "创建时间-开始", example = "2025-01-01")
  private String createTimeStart;

  /**
   * 创建时间-结束
   *
   * <p>可选项，查询在此时间之前创建的用户
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
   * <p>可选项，支持：createTime, updateTime, username
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
