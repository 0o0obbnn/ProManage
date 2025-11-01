package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document file information DTO
 * Contains file-related metadata for documents
 *
 * @author ProManage Team
 * @date 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFileInfo {

    /**
     * File path or URL
     */
    private String filePath;

    /**
     * File name
     */
    private String fileName;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * File extension (e.g., .pdf, .doc, .md)
     */
    private String fileExtension;

    /**
     * Document version
     */
    private Integer version;

    /**
     * File content (for text-based documents)
     */
    private String content;

    /**
     * Content MIME type
     */
    private String contentType;
}
