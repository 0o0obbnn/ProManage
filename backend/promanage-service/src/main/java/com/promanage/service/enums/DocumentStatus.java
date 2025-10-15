package com.promanage.service.enums;

import lombok.Getter;

/**
 * 文档状态枚举
 */
@Getter
public enum DocumentStatus {
    DRAFT(0, "草稿", "DRAFT"),
    REVIEWING(1, "审核中", "UNDER_REVIEW"),
    PUBLISHED(2, "已发布", "APPROVED"),
    ARCHIVED(3, "已归档", "ARCHIVED"),
    DEPRECATED(4, "已废弃", "DEPRECATED");

    private final Integer code;
    private final String description;
    private final String apiName;

    DocumentStatus(Integer code, String description, String apiName) {
        this.code = code;
        this.description = description;
        this.apiName = apiName;
    }

    public static DocumentStatus fromCode(Integer code) {
        if (code == null) return null;
        for (DocumentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static DocumentStatus fromApiName(String apiName) {
        if (apiName == null) {
            return null;
        }
        for (DocumentStatus status : values()) {
            if (status.apiName.equalsIgnoreCase(apiName)) {
                return status;
            }
        }
        return null;
    }

    public static String toApiName(Integer code) {
        DocumentStatus status = fromCode(code);
        return status != null ? status.getApiName() : null;
    }

    public static Integer toCode(String apiName) {
        DocumentStatus status = fromApiName(apiName);
        return status != null ? status.getCode() : null;
    }
}
