package com.promanage.service.impl;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.promanage.infrastructure.cache.CacheService;
import com.promanage.service.entity.Document;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.service.IDocumentViewCountService;

import org.springframework.dao.DataAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentViewCountServiceImpl implements IDocumentViewCountService {

  private final DocumentMapper documentMapper;
  private final CacheService cacheService;

  private static final long VIEW_COUNT_PERSIST_THRESHOLD = 100L;
  private static final String VIEW_COUNT_CACHE_KEY_PREFIX = "document:viewcount:";

  @Override
  public void incrementViewCount(Long documentId) {
    String totalCacheKey = VIEW_COUNT_CACHE_KEY_PREFIX + documentId;
    String today = LocalDate.now().toString();
    String dailyCacheKey = VIEW_COUNT_CACHE_KEY_PREFIX + "daily:" + documentId + ":" + today;

    try {
      Long newViewCount = cacheService.increment(totalCacheKey, 1L);

      if (newViewCount == null) {
        Document document = documentMapper.selectById(documentId);
        Integer currentCount =
            (document != null && document.getViewCount() != null) ? document.getViewCount() : 0;
        newViewCount = (long) (currentCount + 1);
        cacheService.set(totalCacheKey, newViewCount, Duration.ofDays(30));
      }

      cacheService.increment(dailyCacheKey, 1L);
      cacheService.expire(dailyCacheKey, Duration.ofDays(8));

      if (newViewCount % VIEW_COUNT_PERSIST_THRESHOLD == 0) {
        asyncPersistViewCount(documentId, newViewCount);
      }

      log.debug("Incremented document view count, id={}, newCount={}", documentId, newViewCount);

    } catch (IllegalArgumentException e) {
      log.error("Redis failed to increment view count, falling back to DB, id={}", documentId, e);
      try {
        documentMapper.incrementViewCount(documentId);
      } catch (DataAccessException dbError) {
        log.error(
            "Database fallback for view count increment also failed, id={}", documentId, dbError);
      }
    }
  }

  @Override
  public int getWeeklyViewCount(Long documentId) {
    log.debug("Getting weekly view count for documentId={}", documentId);
    LocalDate today = LocalDate.now();
    int weekViewCount = 0;

    try {
      for (int i = 0; i < 7; i++) {
        String dateKey = today.minusDays(i).toString();
        String cacheKey = VIEW_COUNT_CACHE_KEY_PREFIX + "daily:" + documentId + ":" + dateKey;
        Long dailyCount = cacheService.get(cacheKey, Long.class);
        if (dailyCount != null) {
          weekViewCount += dailyCount.intValue();
        }
      }
    } catch (IllegalArgumentException e) {
      log.error("Failed to get weekly view count from Redis, falling back to DB estimate.", e);
      Document document = documentMapper.selectById(documentId);
      if (document == null || document.getViewCount() == null) {
        return 0;
      }
      // Fallback: estimate as 30% of total views
      return (int) (document.getViewCount() * 0.3);
    }

    return weekViewCount;
  }

  @Override
  public Integer getViewCount(Long documentId) {
    String totalCacheKey = VIEW_COUNT_CACHE_KEY_PREFIX + documentId;
    try {
      Long count = cacheService.get(totalCacheKey, Long.class);
      return count != null ? count.intValue() : null;
    } catch (IllegalArgumentException e) {
      log.error("Failed to get view count from Redis for documentId={}", documentId, e);
      return null;
    }
  }

  @Async
  protected void asyncPersistViewCount(Long documentId, Long viewCount) {
    try {
      log.info("Persisting document view count to DB, id={}, viewCount={}", documentId, viewCount);
      Document document = new Document();
      document.setId(documentId);
      document.setViewCount(viewCount.intValue());

      int updated = documentMapper.updateById(document);

      if (updated > 0) {
        log.debug("Successfully persisted view count, id={}, viewCount={}", documentId, viewCount);
        // Evict document cache to reflect new view count on next read
        String cacheKey = "documents::" + documentId;
        cacheService.delete(cacheKey);
      } else {
        log.warn("Failed to persist view count, document may have been deleted, id={}", documentId);
      }
    } catch (DataAccessException e) {
      log.error(
          "Async persistence of view count failed, id={}, viewCount={}", documentId, viewCount, e);
    }
  }
}
