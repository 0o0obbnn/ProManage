package com.promanage.service.service;

/**
 * Service for managing document view counts.
 * <p>
 * This service centralizes the logic for incrementing, persisting, and retrieving
 * document view statistics, using a combination of Redis for performance and
 * database persistence.
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
public interface IDocumentViewCountService {

    /**
     * Atomically increments the view count for a given document.
     *
     * @param documentId The ID of the document to increment the view count for.
     */
    void incrementViewCount(Long documentId);

    /**
     * Retrieves the view count for the last 7 days.
     *
     * @param documentId The ID of the document.
     * @return The total view count for the past week.
     */
    int getWeeklyViewCount(Long documentId);

    /**
     * Retrieves the current view count for a document, typically from the cache.
     *
     * @param documentId The ID of the document.
     * @return The current view count.
     */
    Integer getViewCount(Long documentId);
}
