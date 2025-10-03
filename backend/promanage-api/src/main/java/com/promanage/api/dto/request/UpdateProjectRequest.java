package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新项目请求DTO
 * <p>
 * 用于更新项目信息，所有字段都是可选的
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Schema(description = "更新项目请求")
public class UpdateProjectRequest {

    /**
     * 项目名称
     * <p>
     * 可选项，长度限制为1-100个字符
     * </p>
     */
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    @Schema(description = "项目名称", example = "ProManage项目管理系统v2.0")
    private String name;

    /**
     * 项目描述
     * <p>
     * 可选项，最大长度500个字符
     * </p>
     */
    @Size(max = 500, message = "项目描述长度不能超过500个字符")
    @Schema(description = "项目描述", example = "更新后的项目描述")
    private String description;

    /**
     * 项目状态
     * <p>
     * 可选项，0-未开始，1-进行中，2-已完成，3-已暂停，4-已取消
     * </p>
     */
    @Schema(description = "项目状态：0-未开始，1-进行中，2-已完成，3-已暂停，4-已取消", example = "1")
    private Integer status;

    /**
     * 计划开始日期
     * <p>
     * 可选项，项目计划开始的日期
     * </p>
     */
    @Schema(description = "计划开始日期", example = "2025-01-01")
    private LocalDate startDate;

    /**
     * 计划结束日期
     * <p>
     * 可选项，项目计划结束的日期
     * </p>
     */
    @Schema(description = "计划结束日期", example = "2025-12-31")
    private LocalDate endDate;

    /**
     * 实际开始日期
     * <p>
     * 可选项，项目实际开始的日期
     * </p>
     */
    @Schema(description = "实际开始日期", example = "2025-01-05")
    private LocalDate actualStartDate;

    /**
     * 实际结束日期
     * <p>
     * 可选项，项目实际结束的日期
     * </p>
     */
    @Schema(description = "实际结束日期", example = "2025-11-30")
    private LocalDate actualEndDate;

    /**
     * 项目图标
     * <p>
     * 可选项，项目图标的URL或图标名称
     * </p>
     */
    @Size(max = 200, message = "项目图标长度不能超过200个字符")
    @Schema(description = "项目图标", example = "updated-icon.png")
    private String icon;

    /**
     * 项目颜色
     * <p>
     * 可选项，用于UI展示的项目主题色
     * </p>
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，应为#RRGGBB格式")
    @Schema(description = "项目颜色（十六进制）", example = "#52C41A")
    private String color;

    /**
     * 项目类型
     * <p>
     * 可选项，项目类型：WEB-Web项目，APP-移动应用，SYSTEM-系统软件，OTHER-其他
     * </p>
     */
    @Schema(description = "项目类型：WEB, APP, SYSTEM, OTHER", example = "WEB")
    private String type;

    /**
     * 项目优先级
     * <p>
     * 可选项，1-低，2-中，3-高，4-紧急
     * </p>
     */
    @Schema(description = "项目优先级：1-低，2-中，3-高，4-紧急", example = "3")
    private Integer priority;

    /**
     * 项目进度百分比
     * <p>
     * 可选项，0-100之间的整数
     * </p>
     */
    @Schema(description = "项目进度百分比（0-100）", example = "65")
    private Integer progress;
}