package com.promanage.service.service;

/**
 * Service for managing document favorites.
 *
 * <p>This service handles all business logic related to users favoriting or unfavoriting documents.
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
public interface IDocumentFavoriteService {

  /**
   * Toggles the favorite status of a document for a user.
   *
   * @param documentId The ID of the document.
   * @param userId The ID of the user.
   * @param favorite True to favorite, false to unfavorite.
   */
  void toggleFavorite(Long documentId, Long userId, boolean favorite);

  /**
   * Checks if a document is favorited by a user.
   *
   * @param documentId The ID of the document.
   * @param userId The ID of the user.
   * @return True if the document is favorited, false otherwise.
   */
  boolean isFavorited(Long documentId, Long userId);

  /**
   * Gets the total number of favorites for a document.
   *
   * @param documentId The ID of the document.
   * @return The total favorite count.
   */
  int getFavoriteCount(Long documentId);
}
