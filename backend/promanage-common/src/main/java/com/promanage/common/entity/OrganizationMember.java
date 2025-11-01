package com.promanage.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织成员实体类（临时创建以修复编译错误）
 *
 * @author ProManage Team
 * @date 2025-10-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_organization_member")
public class OrganizationMember extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableField("organization_id")
    private Long organizationId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role;

    @TableField("status")
    private String status;

    @TableField("inviter_id")
    private Long inviterId;

    @TableField("join_time")
    private java.time.LocalDateTime joinTime;
}
