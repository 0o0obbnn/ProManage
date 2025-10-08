package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务活动实体类
 * <p>
 * 记录任务相关的活动历史，如状态变更、评论添加等
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_task_activity")
@Schema(description = "任务活动信息")
public class TaskActivity extends BaseEntity {

    /**
     * 活动ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "活动ID", example = "1")
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
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    /**
     * 活动类型
     * CREATE-创建任务, UPDATE-更新任务, COMMENT-评论, STATUS_CHANGE-状态变更,
     * ASSIGN-分配任务, ATTACHMENT_ADD-添加附件, ATTACHMENT_REMOVE-删除附件
     */
    @Schema(description = "活动类型", example = "STATUS_CHANGE")
    private String activityType;

    /**
     * 活动内容/描述
     */
    @Schema(description = "活动内容/描述", example = "将任务状态从'进行中'变更为'已完成'")
    private String content;

    /**
     * 旧值（用于记录变更前的值）
     */
    @Schema(description = "旧值", example = "进行中")
    private String oldValue;

    /**
     * 新值（用于记录变更后的值）
     */
    @Schema(description = "新值", example = "已完成")
    private String newValue;
}