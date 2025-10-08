package com.promanage.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索结果DTO
 * 包含额外的关联字段信息
 */
@Data
@Schema(description = "搜索结果DTO")
public class SearchResultDTO {

    /**
     * ID
     */
    @Schema(description = "ID", example = "1")
    private Long id;

    /**
     * 标题
     */
    @Schema(description = "标题", example = "项目计划文档")
    private String title;

    /**
     * 内容/描述
     */
    @Schema(description = "内容/描述", example = "这是项目的详细计划")
    private String content;

    /**
     * 项目ID（仅用于文档和任务）
     */
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long creatorId;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称", example = "张三")
    private String creatorName;

    /**
     * 项目名称（仅用于文档和任务）
     */
    @Schema(description = "项目名称", example = "ProManage系统")
    private String projectName;

    /**
     * 指派人ID（仅用于任务）
     */
    @Schema(description = "指派人ID", example = "2")
    private Long assigneeId;

    /**
     * 指派人名称（仅用于任务）
     */
    @Schema(description = "指派人名称", example = "李四")
    private String assigneeName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-09-30 10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2025-09-30 10:00:00")
    private LocalDateTime updateTime;
}