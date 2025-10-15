package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 项目实体类
 * <p>
 * 项目信息实体，包含项目的基本信息和状态
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_project")
@Schema(description = "项目信息")
public class Project extends BaseEntity {

    /**
     * 项目名称
     */
    @Schema(description = "项目名称", example = "ProManage系统")
    private String name;

    /**
     * 项目编码
     */
    @Schema(description = "项目编码", example = "PROMANAGE_2025")
    private String code;

    /**
     * 项目描述
     */
    @Schema(description = "项目描述", example = "智能项目管理系统")
    private String description;

    /**
     * 项目状态
     * 1-进行中 2-已完成 3-已归档 4-已暂停
     */
    @Schema(description = "项目状态", example = "1")
    private Integer status;

    /**
     * 项目负责人ID
     */
    @Schema(description = "项目负责人ID", example = "1")
    private Long ownerId;

    /**
     * 组织ID
     */
    @Schema(description = "组织ID", example = "1")
    private Long organizationId;

    /**
     * 项目开始日期
     */
    @Schema(description = "项目开始日期", example = "2025-01-01")
    private LocalDate startDate;

    /**
     * 项目结束日期
     */
    @Schema(description = "项目结束日期", example = "2025-12-31")
    private LocalDate endDate;

    /**
     * 项目优先级
     * 1-低 2-中 3-高 4-紧急
     */
    @Schema(description = "项目优先级", example = "2")
    private Integer priority;

    /**
     * 项目进度（百分比）
     */
    @Schema(description = "项目进度", example = "50")
    private Integer progress;

    /**
     * 项目标签
     */
    @Schema(description = "项目标签", example = "管理系统,Spring Boot")
    private String tags;

    /**
     * 项目封面图片URL
     */
    @Schema(description = "项目封面图片URL", example = "https://example.com/project-cover.jpg")
    private String coverImage;

    /**
     * 项目图标
     */
    @Schema(description = "项目图标", example = "project-icon.png")
    private String icon;

    /**
     * 项目颜色（十六进制）
     */
    @Schema(description = "项目颜色（十六进制）", example = "#1890FF")
    private String color;

    /**
     * 项目类型
     */
    @Schema(description = "项目类型：WEB, APP, SYSTEM, OTHER", example = "WEB")
    private String type;
}