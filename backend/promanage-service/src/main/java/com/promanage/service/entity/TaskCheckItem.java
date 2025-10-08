package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务检查项实体类
 * <p>
 * 任务的检查清单项，用于分解任务完成标准
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_check_item")
@Schema(description = "任务检查项信息")
public class TaskCheckItem extends BaseEntity {

    /**
     * 检查项ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "检查项ID", example = "1")
    private Long id;

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    /**
     * 检查项内容
     */
    @Schema(description = "检查项内容", example = "完成前端界面设计")
    private String content;

    /**
     * 是否完成
     */
    @Schema(description = "是否完成", example = "false")
    private Boolean isCompleted;

    /**
     * 完成者ID
     */
    @Schema(description = "完成者ID", example = "1")
    private Long completedById;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private java.time.LocalDateTime completedTime;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;
}