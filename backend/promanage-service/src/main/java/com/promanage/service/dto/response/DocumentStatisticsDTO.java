package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档统计信息DTO（服务层）
 * <p>
 * Service layer DTO for document statistics without API annotations
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
public class DocumentStatisticsDTO {

    /**
     * 总版本数
     */
    private Integer totalVersions;

    /**
     * 总浏览量
     */
    private Integer totalViews;

    /**
     * 本周浏览量
     */
    private Integer weekViews;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 评论数
     */
    private Integer commentCount;
}
