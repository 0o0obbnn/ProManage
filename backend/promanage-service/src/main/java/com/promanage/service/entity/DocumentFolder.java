package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档文件夹实体类
 * <p>
 * 用于组织和管理文档的文件夹结构
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_document_folder")
@Schema(description = "文档文件夹实体")
public class DocumentFolder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文件夹名称 (不能为空)
     */
    @TableField("name")
    @Schema(description = "文件夹名称", example = "技术文档", required = true)
    private String name;

    /**
     * 文件夹描述
     */
    @TableField("description")
    @Schema(description = "文件夹描述", example = "存放所有技术相关文档")
    private String description;

    /**
     * 所属项目ID (不能为空)
     */
    @TableField("project_id")
    @Schema(description = "所属项目ID", example = "1", required = true)
    private Long projectId;

    /**
     * 父文件夹ID
     * <p>
     * 0表示根目录
     * </p>
     */
    @TableField("parent_id")
    @Schema(description = "父文件夹ID", example = "0", defaultValue = "0")
    private Long parentId;

    /**
     * 排序
     */
    @TableField("sort_order")
    @Schema(description = "排序", example = "1", defaultValue = "0")
    private Integer sortOrder;

}