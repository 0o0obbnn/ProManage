package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目响应DTO
 * <p>
 * 返回项目基本信息
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目响应")
public class ProjectResponse {

    @Schema(description = "项目ID", example = "1")
    private Long id;

    @Schema(description = "项目名称", example = "ProManage项目管理系统")
    private String name;

    @Schema(description = "项目编码", example = "PROMANAGE_2025")
    private String code;

    @Schema(description = "项目描述", example = "一个强大的项目和文档管理系统")
    private String description;

    @Schema(description = "项目状态：0-未开始，1-进行中，2-已完成，3-已暂停，4-已取消", example = "1")
    private Integer status;

    @Schema(description = "项目负责人ID", example = "1")
    private Long ownerId;

    @Schema(description = "项目负责人姓名", example = "张三")
    private String ownerName;

    @Schema(description = "项目负责人头像", example = "https://example.com/avatar/user1.jpg")
    private String ownerAvatar;

    @Schema(description = "计划开始日期", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "计划结束日期", example = "2025-12-31")
    private LocalDate endDate;

    @Schema(description = "实际开始日期", example = "2025-01-05")
    private LocalDate actualStartDate;

    @Schema(description = "实际结束日期", example = "2025-11-30")
    private LocalDate actualEndDate;

    @Schema(description = "项目图标", example = "project-icon.png")
    private String icon;

    @Schema(description = "项目颜色", example = "#1890FF")
    private String color;

    @Schema(description = "项目类型：WEB, APP, SYSTEM, OTHER", example = "WEB")
    private String type;

    @Schema(description = "项目优先级：1-低，2-中，3-高，4-紧急", example = "2")
    private Integer priority;

    @Schema(description = "项目进度百分比（0-100）", example = "65")
    private Integer progress;

    @Schema(description = "项目成员数量", example = "10")
    private Integer memberCount;

    @Schema(description = "项目文档数量", example = "25")
    private Integer documentCount;

    @Schema(description = "项目任务数量", example = "30")
    private Integer taskCount;

    @Schema(description = "创建时间", example = "2025-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-09-30T10:30:00")
    private LocalDateTime updateTime;
}