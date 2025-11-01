package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Document user information DTO
 * Contains user-related metadata for documents
 *
 * @author ProManage Team
 * @date 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUserInfo {

    /**
     * Creator user ID
     */
    private Long creatorId;

    /**
     * Creator username (optional, populated by service layer)
     */
    private String creatorName;

    /**
     * Creator avatar URL (optional, populated by service layer)
     */
    private String creatorAvatar;

    /**
     * Updater user ID
     */
    private Long updaterId;

    /**
     * Updater username (optional, populated by service layer)
     */
    private String updaterName;

    /**
     * Updater avatar URL (optional, populated by service layer)
     */
    private String updaterAvatar;
}
