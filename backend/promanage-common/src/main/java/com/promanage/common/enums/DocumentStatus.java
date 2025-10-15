package com.promanage.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档状态枚举
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Getter
@AllArgsConstructor
public enum DocumentStatus {

    /**
     * 草稿
     */
    DRAFT(0, "草稿"),

    /**
     * 待审批
     */
    PENDING_APPROVAL(1, "待审批"),

    /**
     * 已发布
     */
    PUBLISHED(2, "已发布"),

    /**
     * 已归档
     */
    ARCHIVED(3, "已归档");

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
     * @return DocumentStatus
     */
    public static DocumentStatus getByCode(Integer code) {
        for (DocumentStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态字符串获取状态码
     *
     * @param status 状态字符串
     * @return 状态码
     */
    public static Integer toCode(String status) {
        if (status == null) {
            return null;
        }
        for (DocumentStatus ds : values()) {
            if (ds.name().equalsIgnoreCase(status) || ds.getDescription().equalsIgnoreCase(status)) {
                return ds.getCode();
            }
        }
        return null;
    }
}