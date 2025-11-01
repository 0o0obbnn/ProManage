package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document project information DTO
 * Contains project-related metadata for documents
 *
 * @author ProManage Team
 * @date 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProjectInfo {

    /**
     * Project ID
     */
    private Long projectId;

    /**
     * Category ID
     */
    private Long categoryId;

    /**
     * Project name (optional, populated by service layer)
     */
    private String projectName;

    /**
     * Category name (optional, populated by service layer)
     */
    private String categoryName;
}
