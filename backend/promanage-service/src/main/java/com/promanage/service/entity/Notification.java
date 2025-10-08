package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_notification")
public class Notification {

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知类型
     */
    @TableField("type")
    private String type;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 相关数据ID（如项目ID、任务ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 相关数据类型
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}