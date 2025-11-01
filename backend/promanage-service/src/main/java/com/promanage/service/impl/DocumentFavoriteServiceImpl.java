package com.promanage.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.service.entity.DocumentFavorite;
import com.promanage.service.mapper.DocumentFavoriteMapper;
import com.promanage.service.service.IDocumentFavoriteService;
import com.promanage.service.service.IDocumentService;

import org.springframework.dao.DataAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFavoriteServiceImpl implements IDocumentFavoriteService {

  private final DocumentFavoriteMapper documentFavoriteMapper;
  private final IDocumentService documentService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void toggleFavorite(Long documentId, Long userId, boolean favorite) {
    log.info("{}文档, documentId={}, userId={}", favorite ? "收藏" : "取消收藏", documentId, userId);

    // 权限检查 - 验证用户有权访问此文档
    // Note: documentService.getById will throw an exception if the user doesn't have access
    documentService.getById(documentId, userId, false);

    if (favorite) {
      favoriteDocument(documentId, userId);
    } else {
      unfavoriteDocument(documentId, userId);
    }
  }

  @Override
  public boolean isFavorited(Long documentId, Long userId) {
    log.info("检查文档是否已收藏, documentId={}, userId={}", documentId, userId);
    try {
      int count = documentFavoriteMapper.countByDocumentIdAndUserId(documentId, userId);
      boolean isFavorited = count > 0;
      log.info("文档收藏状态: {}, documentId={}, userId={}", isFavorited, documentId, userId);
      return isFavorited;
    } catch (DataAccessException e) {
      log.error("检查文档收藏状态失败, documentId={}, userId={}", documentId, userId, e);
      return false;
    }
  }

  @Override
  public int getFavoriteCount(Long documentId) {
    log.info("获取文档收藏数量, documentId={}", documentId);
    try {
      int count = documentFavoriteMapper.countByDocumentId(documentId);
      log.info("文档收藏数量: {}, documentId={}", count, documentId);
      return count;
    } catch (DataAccessException e) {
      log.error("获取文档收藏数量失败, documentId={}", documentId, e);
      return 0;
    }
  }

  private void favoriteDocument(Long documentId, Long userId) {
    // 检查是否已经收藏
    int existingCount = documentFavoriteMapper.countByDocumentIdAndUserId(documentId, userId);
    if (existingCount > 0) {
      log.warn("文档已经被收藏, documentId={}, userId={}", documentId, userId);
      // This is not an exceptional case, just return.
      return;
    }

    // 创建收藏记录
    DocumentFavorite favorite = new DocumentFavorite();
    favorite.setDocumentId(documentId);
    favorite.setUserId(userId);
    favorite.setCreatedAt(LocalDateTime.now());

    documentFavoriteMapper.insert(favorite);
    log.info("文档收藏成功, documentId={}, userId={}", documentId, userId);
  }

  private void unfavoriteDocument(Long documentId, Long userId) {
    // 删除收藏记录
    int deletedCount = documentFavoriteMapper.deleteByDocumentIdAndUserId(documentId, userId);
    if (deletedCount > 0) {
      log.info("取消收藏成功, documentId={}, userId={}", documentId, userId);
    } else {
      log.warn("取消收藏失败, 收藏记录不存在, documentId={}, userId={}", documentId, userId);
    }
  }
}
