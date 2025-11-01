package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.constant.DocumentConstants;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentFavorite;
import com.promanage.service.mapper.DocumentFavoriteMapper;
import com.promanage.service.mapper.DocumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档收藏策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class DocumentFavoriteStrategy {

    private final DocumentFavoriteMapper documentFavoriteMapper;
    private final DocumentMapper documentMapper;

    /**
     * 切换收藏状态
     */
    public void toggleFavorite(Long documentId, Long userId, boolean favorite) {
        // 验证文档存在
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getDocumentId, documentId);
        wrapper.eq(DocumentFavorite::getUserId, userId);
        
        DocumentFavorite existingFavorite = documentFavoriteMapper.selectOne(wrapper);
        
        if (favorite) {
            // 添加收藏
            if (existingFavorite == null) {
                DocumentFavorite newFavorite = new DocumentFavorite();
                newFavorite.setDocumentId(documentId);
                newFavorite.setUserId(userId);
                newFavorite.setCreateTime(LocalDateTime.now());
                documentFavoriteMapper.insert(newFavorite);
            }
        } else {
            // 取消收藏
            if (existingFavorite != null) {
                documentFavoriteMapper.delete(wrapper);
            }
        }
    }

    /**
     * 获取收藏数量
     */
    public int getFavoriteCount(Long documentId, Long userId) {
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getDocumentId, documentId);
        return Math.toIntExact(documentFavoriteMapper.selectCount(wrapper)); // Long → int
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long documentId, Long userId) {
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getDocumentId, documentId);
        wrapper.eq(DocumentFavorite::getUserId, userId);
        return documentFavoriteMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取用户的收藏文档列表
     */
    public List<DocumentFavorite> getUserFavorites(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getUserId, userId);
        wrapper.orderByDesc(DocumentFavorite::getCreateTime);
        
        // 这里应该使用分页查询，简化实现
        return documentFavoriteMapper.selectList(wrapper);
    }

    /**
     * 获取文档的收藏用户列表
     */
    public List<DocumentFavorite> getDocumentFavorites(Long documentId, int page, int pageSize) {
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getDocumentId, documentId);
        wrapper.orderByDesc(DocumentFavorite::getCreateTime);
        
        // 这里应该使用分页查询，简化实现
        return documentFavoriteMapper.selectList(wrapper);
    }

    /**
     * 批量取消收藏
     */
    public int batchUnfavorite(List<Long> documentIds, Long userId) {
        if (documentIds == null || documentIds.isEmpty()) {
            return 0;
        }
        
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getUserId, userId);
        wrapper.in(DocumentFavorite::getDocumentId, documentIds);
        
        return documentFavoriteMapper.delete(wrapper);
    }

    /**
     * 删除文档的所有收藏记录
     */
    public void deleteAllFavorites(Long documentId) {
        LambdaQueryWrapper<DocumentFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFavorite::getDocumentId, documentId);
        documentFavoriteMapper.delete(wrapper);
    }
}
