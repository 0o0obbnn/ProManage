package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档版本响应DTO（服务层）
 * <p>
 * Service layer DTO for document version without API annotations
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionDTO {

    /**
     * 版本ID
     */
    private Long id;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 版本标题
     */
    private String title;

    /**
     * 变更说明
     */
    private String changeLog;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 内容哈希值
     */
    private String contentHash;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者姓名
     */
    private String creatorName;

    /**
     * 创建者头像
     */
    private String creatorAvatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否为当前版本
     */
    private Boolean isCurrent;
}
