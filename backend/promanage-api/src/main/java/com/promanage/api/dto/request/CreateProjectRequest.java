package com.promanage.api.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建项目请求DTO
 *
 * <p>用于创建新项目，包含项目基本信息和计划时间
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "创建项目请求")
public class CreateProjectRequest {

  /**
   * 项目名称
   *
   * <p>必填项，长度限制为1-100个字符
   */
  @NotBlank(message = "项目名称不能为空")
  @Size(max = 100, message = "项目名称长度不能超过100个字符")
  @Schema(
      description = "项目名称",
      example = "ProManage项目管理系统",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  /**
   * 项目编码
   *
   * <p>必填项，长度限制为1-50个字符 只能包含大写字母、数字和下划线，必须唯一
   */
  @NotBlank(message = "项目编码不能为空")
  @Size(max = 50, message = "项目编码长度不能超过50个字符")
  @Pattern(regexp = "^[A-Z0-9_]+$", message = "项目编码只能包含大写字母、数字和下划线")
  @Schema(
      description = "项目编码",
      example = "PROMANAGE_2025",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String code;

  /**
   * 项目描述
   *
   * <p>可选项，最大长度500个字符
   */
  @Size(max = 500, message = "项目描述长度不能超过500个字符")
  @Schema(description = "项目描述", example = "一个强大的项目和文档管理系统，支持多用户协作")
  private String description;

  /**
   * 计划开始日期
   *
   * <p>必填项，项目计划开始的日期
   */
  @NotNull(message = "计划开始日期不能为空")
  @Schema(
      description = "计划开始日期",
      example = "2025-01-01",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDate startDate;

  /**
   * 计划结束日期
   *
   * <p>可选项，项目计划结束的日期，必须晚于开始日期
   */
  @Schema(description = "计划结束日期", example = "2025-12-31")
  private LocalDate endDate;

  /**
   * 项目图标
   *
   * <p>可选项，项目图标的URL或图标名称
   */
  @Size(max = 200, message = "项目图标长度不能超过200个字符")
  @Schema(description = "项目图标", example = "project-icon.png")
  private String icon;

  /**
   * 项目颜色
   *
   * <p>可选项，用于UI展示的项目主题色，支持十六进制颜色码
   */
  @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，应为#RRGGBB格式")
  @Schema(description = "项目颜色（十六进制）", example = "#1890FF")
  private String color;

  /**
   * 项目类型
   *
   * <p>可选项，项目类型：WEB-Web项目，APP-移动应用，SYSTEM-系统软件，OTHER-其他
   */
  @Schema(description = "项目类型：WEB, APP, SYSTEM, OTHER", example = "WEB")
  private String type;

  /**
   * 项目优先级
   *
   * <p>可选项，1-低，2-中（默认），3-高，4-紧急
   */
  @Schema(description = "项目优先级：1-低，2-中，3-高，4-紧急", example = "2")
  private Integer priority;
}
