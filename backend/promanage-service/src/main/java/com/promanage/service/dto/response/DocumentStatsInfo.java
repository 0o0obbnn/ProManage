package com.promanage.service.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document statistics information DTO
 * Contains engagement metrics and statistics for documents
 *
 * @author ProManage Team
 * @date 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatsInfo {

    /**
     * Total view count
     */
    private Integer viewCount;

    /**
     * Favorite count (optional, populated by service layer)
     */
    private Integer favoriteCount;

    /**
     * Whether current user has favorited this document (optional)
     */
    private Boolean isFavorited;

    /**
     * Document tags
     */
    private List<String> tags;

    /**
     * Comment count (optional, populated by service layer)
     */
    private Integer commentCount;

    /**
     * Version count (optional, populated by service layer)
     */
    private Integer versionCount;
}
