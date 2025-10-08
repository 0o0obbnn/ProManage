package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_activity")
public class ProjectActivity extends BaseEntity {

    private Long projectId;

    private Long userId;

    private String activityType;

    private String content;
}
