package com.promanage.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有实体类都应该继承此基类,提供统一的公共字段
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@Schema(description = "实体基类")
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-09-30 10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2025-09-30 10:00:00")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识 (0-未删除, 1-已删除)
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    /**
     * 删除时间
     */
    @TableField(fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "删除时间", example = "2025-09-30 10:00:00")
    private LocalDateTime deletedAt;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", example = "1")
    private Long creatorId;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", example = "1")
    private Long updaterId;

    /**
     * 版本号 (乐观锁)
     * <p>
     * MyBatis-Plus会在更新时自动比较版本号,如果版本号不一致则更新失败
     * 每次更新成功后版本号自动加1
     * </p>
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "版本号", example = "1")
    private Long version;
}