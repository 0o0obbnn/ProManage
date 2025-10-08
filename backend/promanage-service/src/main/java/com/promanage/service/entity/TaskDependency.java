package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务依赖关系实体类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@TableName("tb_task_dependency")
@Schema(description = "任务依赖关系信息")
public class TaskDependency {

    /**
     * 依赖关系ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "依赖关系ID", example = "1")
    private Long id;

    /**
     * 前置任务ID（被依赖的任务）
     */
    @Schema(description = "前置任务ID（被依赖的任务）", example = "1")
    private Long prerequisiteTaskId;

    /**
     * 依赖任务ID（依赖其他任务的任务）
     */
    @Schema(description = "依赖任务ID（依赖其他任务的任务）", example = "2")
    private Long dependentTaskId;

    /**
     * 依赖类型
     * FINISH_TO_START - 完成-开始（默认）
     * START_TO_START - 开始-开始
     * FINISH_TO_FINISH - 完成-完成
     * START_TO_FINISH - 开始-完成
     */
    @Schema(description = "依赖类型", example = "FINISH_TO_START", 
            allowableValues = {"FINISH_TO_START", "START_TO_START", "FINISH_TO_FINISH", "START_TO_FINISH"})
    private String dependencyType;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-10-03T10:00:00")
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long creatorId;
}

