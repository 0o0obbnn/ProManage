package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.constant.DocumentConstants;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentVersionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档版本管理策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class DocumentVersionStrategy {

    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentMapper documentMapper;

    /**
     * 获取文档版本列表
     */
    public List<DocumentVersion> listVersions(Long documentId, Long userId) {
        LambdaQueryWrapper<DocumentVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentVersion::getDocumentId, documentId);
        wrapper.orderByDesc(DocumentVersion::getCreateTime);
        return documentVersionMapper.selectList(wrapper);
    }

    /**
     * 获取指定版本
     */
    public DocumentVersion getVersion(Long documentId, String version, Long userId) {
        LambdaQueryWrapper<DocumentVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentVersion::getDocumentId, documentId);
        wrapper.eq(DocumentVersion::getVersionNumber, version);
        return documentVersionMapper.selectOne(wrapper);
    }

    /**
     * 创建文档版本
     */
    public Long createVersion(DocumentVersion documentVersion, Long creatorId) {
        // 验证文档存在
        Document document = documentMapper.selectById(documentVersion.getDocumentId());
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        // 设置版本信息
        documentVersion.setCreatorId(creatorId);
        documentVersion.setCreateTime(LocalDateTime.now());
        
        // 生成版本号
        if (documentVersion.getVersionNumber() == null || documentVersion.getVersionNumber().trim().isEmpty()) {
            documentVersion.setVersionNumber(generateVersionNumber(documentVersion.getDocumentId()));
        }
        
        documentVersionMapper.insert(documentVersion);
        return documentVersion.getId();
    }

    /**
     * 回滚到指定版本
     */
    public Document rollbackToVersion(Long documentId, String version, Long updaterId) {
        // 获取文档
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        // 获取目标版本
        DocumentVersion targetVersion = getVersion(documentId, version, updaterId);
        if (targetVersion == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "版本不存在");
        }
        
        // 创建新版本（当前状态）
        DocumentVersion currentVersion = new DocumentVersion();
        currentVersion.setDocumentId(documentId);
        currentVersion.setVersionNumber(generateVersionNumber(documentId));
        currentVersion.setContent(document.getContent());
        currentVersion.setContentType(document.getContentType());
        currentVersion.setCreatorId(updaterId);
        currentVersion.setCreateTime(LocalDateTime.now());
        currentVersion.setChangeLog("回滚前备份");
        documentVersionMapper.insert(currentVersion);
        
        // 回滚文档内容
        document.setContent(targetVersion.getContent());
        document.setContentType(targetVersion.getContentType());
        document.setCurrentVersion(targetVersion.getVersionNumber());
        document.setUpdaterId(updaterId);
        document.setUpdateTime(LocalDateTime.now());
        documentMapper.updateById(document);
        
        return document;
    }

    /**
     * 获取文档版本数量
     */
    public int getVersionCount(Long documentId) {
        LambdaQueryWrapper<DocumentVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentVersion::getDocumentId, documentId);
        return Math.toIntExact(documentVersionMapper.selectCount(wrapper)); // Long → int
    }

    /**
     * 删除文档的所有版本
     */
    public void deleteAllVersions(Long documentId) {
        LambdaQueryWrapper<DocumentVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentVersion::getDocumentId, documentId);
        documentVersionMapper.delete(wrapper);
    }

    /**
     * 生成版本号
     */
    private String generateVersionNumber(Long documentId) {
        int versionCount = getVersionCount(documentId);
        return String.format("v%d.%d", (versionCount / 10) + 1, (versionCount % 10) + 1);
    }
}
