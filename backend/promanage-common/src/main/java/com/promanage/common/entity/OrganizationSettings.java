package com.promanage.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织设置实体类（临时创建以修复编译错误）
 *
 * @author ProManage Team
 * @date 2025-10-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_organization_settings")
public class OrganizationSettings extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("organization_id")
    private Long organizationId;

    @TableField("setting_key")
    private String settingKey;

    @TableField("setting_value")
    private String settingValue;

    @TableField("setting_type")
    private String settingType;

    @TableField("allow_public_projects")
    private Boolean allowPublicProjects;

    @TableField("max_projects")
    private Integer maxProjects;

    @TableField("max_members")
    private Integer maxMembers;
}
