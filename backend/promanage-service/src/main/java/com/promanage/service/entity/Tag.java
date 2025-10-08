package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.promanage.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * <p>
 * 文档标签信息实体，用于文档分类和检索
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tags")
@Schema(description = "标签信息")
public class Tag extends BaseEntity {

    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "标签ID", example = "1")
    private Long id;

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "技术文档")
    private String name;

    /**
     * 标签颜色（十六进制颜色代码）
     */
    @Schema(description = "标签颜色", example = "#1890ff")
    private String color;

    /**
     * 标签描述
     */
    @Schema(description = "标签描述", example = "技术相关文档标签")
    private String description;

    /**
     * 项目ID（null表示全局标签）
     */
    @Schema(description = "项目ID", example = "1")
    private Long projectId;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long creatorId;

    /**
     * 使用次数
     */
    @Schema(description = "使用次数", example = "10")
    private Integer usageCount;

    /**
     * 是否激活
     */
    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "逻辑删除时间")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @Schema(description = "删除人ID", example = "1")
    private Long deletedBy;
}