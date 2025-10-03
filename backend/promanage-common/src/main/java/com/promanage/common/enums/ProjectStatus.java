package com.promanage.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目状态枚举
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Getter
@AllArgsConstructor
public enum ProjectStatus {

    /**
     * 活跃
     */
    ACTIVE(1, "活跃"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),

    /**
     * 已归档
     */
    ARCHIVED(3, "已归档"),

    /**
     * 已暂停
     */
    SUSPENDED(4, "已暂停");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return ProjectStatus
     */
    public static ProjectStatus getByCode(Integer code) {
        for (ProjectStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}