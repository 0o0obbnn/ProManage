package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document basic information DTO
 * Contains core document metadata
 *
 * @author ProManage Team
 * @date 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentBasicInfo {

    /**
     * Document ID
     */
    private Long id;

    /**
     * Document title
     */
    private String title;

    /**
     * Document description
     */
    private String description;

    /**
     * Document content
     */
    private String content;

    /**
     * Content type (e.g., text/html, text/markdown)
     */
    private String contentType;

    /**
     * Document summary
     */
    private String summary;

    /**
     * Document type (e.g., requirement, design, api, user_guide)
     */
    private String type;

    /**
     * Document status (e.g., draft, published, archived)
     */
    private String status;

    /**
     * Current version number
     */
    private Integer version;
}
