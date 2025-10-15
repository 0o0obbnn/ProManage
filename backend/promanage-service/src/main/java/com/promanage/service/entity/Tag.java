package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
     * 使用次数
     */
    @Schema(description = "使用次数", example = "10")
    private Integer usageCount;

    /**
     * 是否激活
     */
    @Schema(description = "是否激活", example = "true")
    private Boolean isActive;

}