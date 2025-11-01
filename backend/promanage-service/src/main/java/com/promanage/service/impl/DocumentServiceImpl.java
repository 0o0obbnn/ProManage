package com.promanage.service.impl;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.common.enums.DocumentStatus;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.response.DocumentBasicInfo;
import com.promanage.service.dto.response.DocumentDownloadInfo;
import com.promanage.service.dto.response.DocumentDTO;
import com.promanage.service.dto.response.DocumentFileInfo;
import com.promanage.service.dto.response.DocumentFolderDTO;
import com.promanage.service.dto.response.DocumentProjectInfo;
import com.promanage.service.dto.response.DocumentStatsInfo;
import com.promanage.service.dto.response.DocumentUserInfo;
import com.promanage.service.dto.request.DocumentSearchRequest;
import com.promanage.service.dto.request.DocumentUploadRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.service.constant.DocumentConstants;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.strategy.DocumentFavoriteStrategy;
import com.promanage.service.strategy.DocumentQueryStrategy;
import com.promanage.service.strategy.DocumentUploadStrategy;
import com.promanage.service.strategy.DocumentVersionStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档服务实现类 - 重构版本
 * 使用策略模式减少方法数量
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements IDocumentService {

    private final DocumentMapper documentMapper;
    
    // 策略类
    private final DocumentQueryStrategy queryStrategy;
    private final DocumentVersionStrategy versionStrategy;
    private final DocumentFavoriteStrategy favoriteStrategy;
    private final DocumentUploadStrategy uploadStrategy;

    // ==================== 核心CRUD方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document createDocument(Long projectId, CreateDocumentRequest request, Long creatorId) {
        validateCreateDocumentRequest(request);
        
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setContent(request.getContent());
        document.setContentType(request.getContentType());
        document.setProjectId(projectId);
        document.setStatus(DocumentStatus.DRAFT.getCode());
        document.setCreatorId(creatorId);
        document.setCreateTime(java.time.LocalDateTime.now());
        document.setUpdateTime(java.time.LocalDateTime.now());
        
        documentMapper.insert(document);
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document updateDocument(Long documentId, UpdateDocumentRequest request, Long updaterId) {
        validateUpdateDocumentRequest(request);
        
        Document document = queryStrategy.getById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }
        if (request.getContentType() != null) {
            document.setContentType(request.getContentType());
        }
        
        document.setUpdaterId(updaterId);
        document.setUpdateTime(java.time.LocalDateTime.now());
        
        documentMapper.updateById(document);
        return document;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long documentId, Long deleterId) {
        Document document = queryStrategy.getById(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        // 删除相关数据
        versionStrategy.deleteAllVersions(documentId);
        favoriteStrategy.deleteAllFavorites(documentId);
        
        documentMapper.deleteById(documentId);
    }

    @Override
    public Document getById(Long id, Long userId, boolean incrementView) {
        Document document = queryStrategy.getById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, DocumentConstants.ERROR_DOCUMENT_NOT_FOUND);
        }
        
        if (incrementView) {
            incrementViewCount(id);
        }
        
        return document;
    }

    // ==================== 查询方法 ====================

    @Override
    public PageResult<Document> listByProject(Long projectId, Integer page, Integer pageSize,
                                            String keyword, String status, String type, Long userId) {
        return queryStrategy.listByProject(projectId, page, pageSize, keyword, status, type, userId);
    }

    @Override
    public List<Document> listByProject(Long projectId, Long userId) {
        return queryStrategy.listByProject(projectId, userId);
    }

    @Override
    public List<Document> listByCreator(Long creatorId, Long userId) {
        return queryStrategy.listByCreator(creatorId, userId);
    }

    @Override
    public PageResult<Document> listAllDocuments(Integer page, Integer pageSize,
                                               Long projectId, String status, String keyword) {
        return queryStrategy.listAllDocuments(
            page != null ? page : 1,
            pageSize != null ? pageSize : 20,
            keyword,
            status,
            null,  // type parameter (not in interface)
            null   // userId parameter (not in interface)
        );
    }

    @Override
    public PageResult<Document> searchDocuments(DocumentSearchRequest request, Long userId) {
        return queryStrategy.searchDocuments(request, userId);
    }

    @Override
    public int countByProject(Long projectId, Long userId) {
        return queryStrategy.countByProject(projectId, userId);
    }

    @Override
    public int countByCreator(Long creatorId, Long userId) {
        return queryStrategy.countByCreator(creatorId, userId);
    }

    @Override
    public java.util.Map<Long, String> getProjectNames(List<Long> projectIds, Long userId) {
        return queryStrategy.getProjectNames(projectIds, userId);
    }

    // ==================== 版本管理方法 ====================

    @Override
    public List<DocumentVersion> listVersions(Long documentId, Long userId) {
        return versionStrategy.listVersions(documentId, userId);
    }

    @Override
    public DocumentVersion getVersion(Long documentId, String version, Long userId) {
        return versionStrategy.getVersion(documentId, version, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(DocumentVersion documentVersion, Long creatorId) {
        return versionStrategy.createVersion(documentVersion, creatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document rollbackToVersion(Long documentId, String version, Long updaterId) {
        return versionStrategy.rollbackToVersion(documentId, version, updaterId);
    }

    // ==================== 收藏管理方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long documentId, Long userId, boolean favorite) {
        favoriteStrategy.toggleFavorite(documentId, userId, favorite);
    }

    @Override
    public int getFavoriteCount(Long documentId, Long userId) {
        return favoriteStrategy.getFavoriteCount(documentId, userId);
    }

    @Override
    public boolean isFavorited(Long documentId, Long userId) {
        return favoriteStrategy.isFavorited(documentId, userId);
    }

    // ==================== 上传下载方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Document upload(DocumentUploadRequest request, Long uploaderId) throws IOException {
        return uploadStrategy.upload(request, uploaderId);
    }

    @Override
    public DocumentDownloadInfo getDownloadInfo(Long id, Long userId) {
        return uploadStrategy.getDownloadInfo(id, userId);
    }

    @Override
    public void downloadDocument(Long id, Long userId, HttpServletResponse response) {
        // 这里应该实现文件下载逻辑
        // 简化实现
        log.info("下载文档, id={}, userId={}", id, userId);
    }

    // ==================== 其他方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Document document) {
        return createDocumentInternal(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Document document, String changeLog) {
        updateDocumentInternal(id, document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 从安全上下文获取当前用户ID
        Long deleterId = com.promanage.infrastructure.security.SecurityUtils.getCurrentUserIdOrThrow();
        deleteDocument(id, deleterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids, Long deleterId) {
        return batchDeleteDocuments(ids, deleterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, DocumentStatus status, Long updaterId) {
        updateDocumentStatus(id, status, updaterId);
    }

    @Override
    public List<DocumentFolderDTO> getFolderTree(Long projectId, Long userId) {
        return java.util.Collections.emptyList();
    }

    @Override
    public int getWeeklyViewCount(Long documentId, Long userId) {
        return 0;
    }

    @Override
    public DocumentDTO uploadDocument(Long projectId, Long uploaderId,
                                    DocumentUploadRequest metadata,
                                    org.springframework.web.multipart.MultipartFile file) {
        try {
            Document document = uploadStrategy.upload(metadata, uploaderId);
            return convertToDTO(document);
        } catch (IOException e) {
            log.error("上传文档失败, projectId={}, uploaderId={}", projectId, uploaderId, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "上传文档失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    private void validateCreateDocumentRequest(CreateDocumentRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档信息不能为空");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档标题不能为空");
        }
    }

    private void validateUpdateDocumentRequest(UpdateDocumentRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档信息不能为空");
        }
        if (request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, DocumentConstants.ERROR_DOCUMENT_ID_NULL);
        }
    }

    private void incrementViewCount(Long documentId) {
        // 这里应该实现访问量增加逻辑
        // 简化实现
        log.info("增加文档访问量, documentId={}", documentId);
    }

    private int batchDeleteDocuments(List<Long> ids, Long deleterId) {
        int deletedCount = 0;
        for (Long id : ids) {
            try {
                deleteDocument(id, deleterId);
                deletedCount++;
            } catch (DataAccessException e) {
                log.error("批量删除文档失败, id={}", id, e);
            }
        }
        return deletedCount;
    }

    private Long createDocumentInternal(Document document) {
        document.setCreateTime(java.time.LocalDateTime.now());
        document.setUpdateTime(java.time.LocalDateTime.now());
        documentMapper.insert(document);
        return document.getId();
    }

    private void updateDocumentInternal(Long id, Document document) {
        document.setId(id);
        document.setUpdateTime(java.time.LocalDateTime.now());
        documentMapper.updateById(document);
    }

    private DocumentDTO convertToDTO(Document document) {
        if (document == null) {
            return null;
        }

        DocumentBasicInfo basicInfo = DocumentBasicInfo.builder()
            .id(document.getId())
            .title(document.getTitle())
            .description(document.getDescription())
            .content(document.getContent())
            .contentType(document.getContentType())
            .summary(document.getSummary())
            .type(document.getType())
            .status(DocumentStatus.fromCode(document.getStatus()).getDescription())
            .version(parseVersion(document.getCurrentVersion()))
            .build();

        DocumentProjectInfo projectInfo = DocumentProjectInfo.builder()
            .projectId(document.getProjectId())
            .categoryId(document.getCategoryId())
            // projectName will be populated by service layer
            .build();

        DocumentUserInfo userInfo = DocumentUserInfo.builder()
            .creatorId(document.getCreatorId())
            // creatorName will be populated by service layer
            .updaterId(document.getUpdaterId())
            // updaterName will be populated by service layer
            .build();

        DocumentFileInfo fileInfo = DocumentFileInfo.builder()
            .filePath(document.getFileUrl())
            .fileName(document.getTitle())
            .fileSize(document.getFileSize())
            // fileExtension needs to be derived from fileName or filePath
            .version(parseVersion(document.getCurrentVersion()))
            .content(document.getContent())
            .contentType(document.getContentType())
            .build();

        DocumentStatsInfo statsInfo = DocumentStatsInfo.builder()
            .viewCount(document.getViewCount())
            // favoriteCount and isFavorited will be populated by service layer
            // tags are stored in DocumentTag table, not as a field on Document entity
            .tags(java.util.Collections.emptyList())
            .build();

        return DocumentDTO.builder()
            .basicInfo(basicInfo)
            .projectInfo(projectInfo)
            .userInfo(userInfo)
            .fileInfo(fileInfo)
            .statsInfo(statsInfo)
            .createTime(document.getCreateTime())
            .updateTime(document.getUpdateTime())
            .build();
    }

    /**
     * Update document status
     *
     * @param id Document ID
     * @param status New status
     * @param updaterId User ID performing the update
     */
    private void updateDocumentStatus(Long id, DocumentStatus status, Long updaterId) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
        }

        document.setStatus(status.getCode());
        document.setUpdatedBy(updaterId);
        document.setUpdatedAt(java.time.LocalDateTime.now());
        documentMapper.updateById(document);
    }

    /**
     * Parse version string to Integer
     *
     * @param versionStr Version string (e.g., "1", "2.0", "v1")
     * @return Parsed integer version, or 1 if parsing fails
     */
    private Integer parseVersion(String versionStr) {
        if (versionStr == null || versionStr.isEmpty()) {
            return 1;
        }
        try {
            // Remove common version prefixes like 'v' or 'V'
            String cleaned = versionStr.replaceAll("^[vV]", "");
            // Extract integer part (before decimal point if exists)
            String intPart = cleaned.split("\\.")[0];
            return Integer.parseInt(intPart);
        } catch (NumberFormatException e) {
            return 1; // Default to version 1 if parsing fails
        }
    }
}
