package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.cache.CacheService;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文档服务实现类
 * <p>
 * 实现文档管理的所有业务逻辑,包括CRUD操作、版本管理和搜索功能
 * 使用Redis缓存提高查询性能,使用事务保证数据一致性
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements IDocumentService {

    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final CacheService cacheService;

    /**
     * View count persistence threshold
     * Persist to database every 100 views
     */
    private static final long VIEW_COUNT_PERSIST_THRESHOLD = 100L;

    /**
     * Redis key prefix for document view count
     */
    private static final String VIEW_COUNT_CACHE_KEY_PREFIX = "document:viewcount:";

    @Override
    public Document getById(Long id) {
        log.info("查询文档详情, id={}", id);

        // 查询文档
        Document document = getByIdWithoutView(id);

        // 使用 Redis 原子操作增加浏览次数
        try {
            incrementViewCountAtomic(id, document);
        } catch (Exception e) {
            log.error("增加文档浏览次数失败, id={}", id, e);
            // 不影响主流程，继续返回文档
        }

        return document;
    }

    /**
     * 原子性增加浏览次数
     * <p>
     * 使用 Redis INCR 命令保证原子性，避免并发问题。
     * 每100次浏览异步持久化到数据库一次，减少数据库压力。
     * </p>
     *
     * @param documentId 文档ID
     * @param document 文档对象
     */
    private void incrementViewCountAtomic(Long documentId, Document document) {
        String cacheKey = VIEW_COUNT_CACHE_KEY_PREFIX + documentId;

        try {
            // 使用 Redis INCR 原子操作
            Long newViewCount = cacheService.increment(cacheKey, 1L);

            if (newViewCount == null) {
                // 首次访问，初始化 Redis 计数器
                Integer currentCount = document.getViewCount() != null ? document.getViewCount() : 0;
                cacheService.set(cacheKey, currentCount + 1, java.time.Duration.ofSeconds(86400L)); // 24小时过期
                newViewCount = (long) (currentCount + 1);
            }

            // 更新文档对象的浏览次数（用于返回给前端）
            document.setViewCount(newViewCount.intValue());

            // 每100次浏览持久化一次到数据库
            if (newViewCount % VIEW_COUNT_PERSIST_THRESHOLD == 0) {
                asyncPersistViewCount(documentId, newViewCount);
            }

            log.debug("增加文档浏览次数, id={}, newCount={}", documentId, newViewCount);

        } catch (Exception e) {
            log.error("Redis增加浏览次数失败, 回退到数据库操作, id={}", documentId, e);
            // Redis 失败时回退到数据库操作（可能有并发问题，但至少能工作）
            try {
                documentMapper.incrementViewCount(documentId);
            } catch (Exception dbError) {
                log.error("数据库增加浏览次数也失败, id={}", documentId, dbError);
            }
        }
    }

    /**
     * 异步持久化浏览次数到数据库
     * <p>
     * 使用异步方式减少对主流程的影响
     * </p>
     *
     * @param documentId 文档ID
     * @param viewCount 浏览次数
     */
    @Async
    protected void asyncPersistViewCount(Long documentId, Long viewCount) {
        try {
            log.info("持久化文档浏览次数到数据库, id={}, viewCount={}", documentId, viewCount);

            // 直接更新浏览次数
            Document document = new Document();
            document.setId(documentId);
            document.setViewCount(viewCount.intValue());

            int updated = documentMapper.updateById(document);

            if (updated > 0) {
                log.debug("持久化浏览次数成功, id={}, viewCount={}", documentId, viewCount);

                // 清除文档缓存，强制下次查询时从数据库读取最新数据
                String cacheKey = "documents::" + documentId;
                cacheService.delete(cacheKey);
            } else {
                log.warn("持久化浏览次数失败，文档可能已被删除, id={}", documentId);
            }

        } catch (Exception e) {
            log.error("异步持久化浏览次数失败, id={}, viewCount={}", documentId, viewCount, e);
            // 异步操作失败不抛异常，记录日志即可
        }
    }

    @Override
    @Cacheable(value = "documents", key = "#id", unless = "#result == null")
    public Document getByIdWithoutView(Long id) {
        log.info("查询文档详情(不增加浏览次数), id={}", id);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        Document document = documentMapper.selectById(id);
        if (document == null || document.getDeleted()) {
            log.warn("文档不存在, id={}", id);
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档不存在");
        }

        return document;
    }

    @Override
    public PageResult<Document> listDocuments(Integer page, Integer pageSize, String keyword,
                                              Long projectId, String type, Integer status) {
        log.info("分页查询文档列表, page={}, pageSize={}, keyword={}, projectId={}, type={}, status={}",
                page, pageSize, keyword, projectId, type, status);

        // 构建分页对象
        Page<Document> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            queryWrapper.eq(Document::getProjectId, projectId);
        }
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq(Document::getType, type);
        }
        if (status != null) {
            queryWrapper.eq(Document::getStatus, status);
        }
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(w -> w
                .like(Document::getTitle, keyword)
                .or().like(Document::getContent, keyword)
                .or().like(Document::getSummary, keyword)
            );
        }
        queryWrapper.orderByDesc(Document::getUpdateTime);

        // 执行查询
        IPage<Document> result = documentMapper.selectPage(pageParam, queryWrapper);

        log.info("查询文档列表成功, total={}", result.getTotal());
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public PageResult<Document> listDocuments(Long projectId, Integer page, Integer size) {
        return listDocuments(page, size, null, projectId, null, null);
    }

    @Override
    public Document createDocument(Long projectId, CreateDocumentRequest request) {
        // 创建文档实体
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setContentType(request.getContentType());
        document.setSummary(request.getSummary());
        document.setType(request.getType());
        document.setCategoryId(request.getCategoryId());
        document.setProjectId(projectId);
        document.setFolderId(request.getFolderId() != null ? request.getFolderId() : 0L);
        document.setIsTemplate(request.getIsTemplate());
        document.setPriority(request.getPriority());
        document.setCreatorId(1L); // TODO: 从上下文中获取当前用户ID
        
        // 设置标签
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            // TODO: 处理标签
        }
        
        // 创建文档
        Long documentId = create(document);
        
        // 返回创建的文档
        return getById(documentId);
    }

    @Override
    public Document getDocumentById(Long documentId) {
        return getById(documentId);
    }

    @Override
    public Document updateDocument(Long documentId, UpdateDocumentRequest request) {
        // 获取现有文档
        Document existingDocument = getByIdWithoutView(documentId);
        
        // 更新文档字段
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setContentType(request.getContentType());
        document.setSummary(request.getSummary());
        document.setType(request.getType());
        document.setCategoryId(request.getCategoryId());
        document.setFolderId(request.getFolderId());
        document.setPriority(request.getPriority());
        document.setReviewerId(request.getReviewerId());
        document.setUpdaterId(1L); // TODO: 从上下文中获取当前用户ID
        
        // 更新文档
        update(documentId, document, request.getChangelog());
        
        // 返回更新后的文档
        return getById(documentId);
    }

    @Override
    public void deleteDocument(Long documentId) {
        delete(documentId);
    }

    @Override
    public List<Document> listByProjectId(Long projectId) {
        log.info("查询项目文档, projectId={}", projectId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        return documentMapper.findByProjectId(projectId);
    }

    @Override
    public List<Document> listByCreatorId(Long creatorId) {
        log.info("查询用户创建的文档, creatorId={}", creatorId);

        if (creatorId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "创建人ID不能为空");
        }

        return documentMapper.findByCreatorId(creatorId);
    }

    @Override
    public List<Document> searchByKeyword(String keyword) {
        log.info("搜索文档, keyword={}", keyword);

        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }

        return documentMapper.searchByKeyword(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", allEntries = true)
    public Long create(Document document) {
        log.info("创建文档, title={}, projectId={}", document.getTitle(), document.getProjectId());

        // 参数验证
        validateDocument(document, true);

        // 设置默认值
        if (document.getStatus() == null) {
            document.setStatus(0); // 默认草稿状态
        }
        if (StringUtils.isBlank(document.getCurrentVersion())) {
            document.setCurrentVersion("1.0.0");
        }
        if (document.getViewCount() == null) {
            document.setViewCount(0);
        }
        if (document.getFolderId() == null) {
            document.setFolderId(0L); // 默认根目录
        }
        if (document.getIsTemplate() == null) {
            document.setIsTemplate(false); // 默认非模板
        }
        if (StringUtils.isBlank(document.getContentType())) {
            document.setContentType("markdown"); // 默认Markdown格式
        }

        // 保存文档
        documentMapper.insert(document);

        // 创建初始版本
        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersionNumber("1.0.0");
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setContentType(document.getContentType());
        version.setChangeLog("初始版本");
        version.setFileUrl(document.getFileUrl());
        version.setFileSize(document.getFileSize());
        version.setCreatorId(document.getCreatorId());
        version.setIsCurrent(true); // 初始版本为当前版本
        documentVersionMapper.insert(version);

        log.info("创建文档成功, id={}, title={}", document.getId(), document.getTitle());
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void update(Long id, Document document, String changeLog) {
        log.info("更新文档, id={}", id);

        // 检查文档是否存在
        Document existingDocument = getByIdWithoutView(id);

        // 更新字段
        if (StringUtils.isNotBlank(document.getTitle())) {
            existingDocument.setTitle(document.getTitle());
        }
        if (StringUtils.isNotBlank(document.getContent())) {
            existingDocument.setContent(document.getContent());
        }
        if (StringUtils.isNotBlank(document.getContentType())) {
            existingDocument.setContentType(document.getContentType());
        }
        if (StringUtils.isNotBlank(document.getSummary())) {
            existingDocument.setSummary(document.getSummary());
        }
        if (StringUtils.isNotBlank(document.getType())) {
            existingDocument.setType(document.getType());
        }
        if (document.getCategoryId() != null) {
            existingDocument.setCategoryId(document.getCategoryId());
        }
        if (document.getFileUrl() != null) {
            existingDocument.setFileUrl(document.getFileUrl());
        }
        if (document.getFileSize() != null) {
            existingDocument.setFileSize(document.getFileSize());
        }
        if (document.getFolderId() != null) {
            existingDocument.setFolderId(document.getFolderId());
        }
        if (document.getPriority() != null) {
            existingDocument.setPriority(document.getPriority());
        }
        if (document.getReviewerId() != null) {
            existingDocument.setReviewerId(document.getReviewerId());
        }
        if (document.getUpdaterId() != null) {
            existingDocument.setUpdaterId(document.getUpdaterId());
        }

        // 生成新版本号 (简化版本: 1.0.0 -> 1.0.1)
        String newVersion = generateNextVersion(existingDocument.getCurrentVersion());
        existingDocument.setCurrentVersion(newVersion);

        // 保存更新
        documentMapper.updateById(existingDocument);

        // 创建新版本记录
        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(id);
        version.setVersionNumber(newVersion);
        version.setTitle(existingDocument.getTitle());
        version.setContent(existingDocument.getContent());
        version.setContentType(existingDocument.getContentType());
        version.setChangeLog(StringUtils.isNotBlank(changeLog) ? changeLog : "文档更新");
        version.setFileUrl(existingDocument.getFileUrl());
        version.setFileSize(existingDocument.getFileSize());
        version.setCreatorId(document.getUpdaterId() != null ? document.getUpdaterId() : existingDocument.getCreatorId());
        documentVersionMapper.insert(version);

        log.info("更新文档成功, id={}, newVersion={}", id, newVersion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void delete(Long id) {
        log.info("删除文档, id={}", id);

        // 检查文档是否存在
        getByIdWithoutView(id);

        // 逻辑删除文档
        documentMapper.deleteById(id);

        // 注意: 版本历史不删除,保留审计记录

        log.info("删除文档成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", allEntries = true)
    public int batchDelete(List<Long> ids) {
        log.info("批量删除文档, ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int count = documentMapper.deleteByIds(ids);

        log.info("批量删除文档成功, count={}", count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void publish(Long id) {
        log.info("发布文档, id={}", id);

        updateStatus(id, 2); // 状态2表示已发布

        log.info("发布文档成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void archive(Long id) {
        log.info("归档文档, id={}", id);

        updateStatus(id, 3); // 状态3表示已归档

        log.info("归档文档成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void updateStatus(Long id, Integer status) {
        log.info("更新文档状态, id={}, status={}", id, status);

        // 参数验证
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效");
        }

        // 检查文档是否存在
        Document document = getByIdWithoutView(id);

        // 更新状态
        document.setStatus(status);
        documentMapper.updateById(document);

        log.info("更新文档状态成功, id={}, status={}", id, status);
    }

    @Override
    @Cacheable(value = "documentVersions", key = "#documentId")
    public List<DocumentVersion> listVersions(Long documentId) {
        log.info("查询文档版本列表, documentId={}", documentId);

        if (documentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        return documentVersionMapper.findByDocumentId(documentId);
    }

    @Override
    @Cacheable(value = "documentVersions", key = "#documentId + ':' + #version")
    public DocumentVersion getVersion(Long documentId, String version) {
        log.info("查询文档版本, documentId={}, version={}", documentId, version);

        if (documentId == null || StringUtils.isBlank(version)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID和版本号不能为空");
        }

        DocumentVersion documentVersion = documentVersionMapper.findByDocumentIdAndVersion(documentId, version);
        if (documentVersion == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档版本不存在");
        }

        return documentVersion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documentVersions", allEntries = true)
    public Long createVersion(DocumentVersion documentVersion) {
        log.info("创建文档版本, documentId={}, version={}",
                documentVersion.getDocumentId(), documentVersion.getVersionNumber());

        // 参数验证
        if (documentVersion.getDocumentId() == null || StringUtils.isBlank(documentVersion.getVersionNumber())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID和版本号不能为空");
        }

        // 保存版本
        documentVersionMapper.insert(documentVersion);

        log.info("创建文档版本成功, id={}", documentVersion.getId());
        return documentVersion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"documents", "documentVersions"}, allEntries = true)
    public void rollbackToVersion(Long documentId, String version) {
        log.info("回滚文档到指定版本, documentId={}, version={}", documentId, version);

        // 检查文档是否存在
        Document document = getByIdWithoutView(documentId);

        // 获取指定版本
        DocumentVersion targetVersion = getVersion(documentId, version);

        // 恢复内容
        document.setContent(targetVersion.getContent());
        document.setFileUrl(targetVersion.getFileUrl());

        // 生成新版本号
        String newVersion = generateNextVersion(document.getCurrentVersion());
        document.setCurrentVersion(newVersion);

        // 保存文档
        documentMapper.updateById(document);

        // 创建新版本记录
        DocumentVersion newVersionRecord = new DocumentVersion();
        newVersionRecord.setDocumentId(documentId);
        newVersionRecord.setVersionNumber(newVersion);
        newVersionRecord.setContent(targetVersion.getContent());
        newVersionRecord.setChangeLog("回滚到版本 " + version);
        newVersionRecord.setFileUrl(targetVersion.getFileUrl());
        newVersionRecord.setCreatorId(targetVersion.getCreatorId());
        documentVersionMapper.insert(newVersionRecord);

        log.info("回滚文档成功, documentId={}, targetVersion={}, newVersion={}", documentId, version, newVersion);
    }

    @Override
    public int countByProjectId(Long projectId) {
        log.info("统计项目文档数量, projectId={}", projectId);

        if (projectId == null) {
            return 0;
        }

        return documentMapper.countByProjectId(projectId);
    }

    @Override
    public int countByCreatorId(Long creatorId) {
        log.info("统计用户创建的文档数量, creatorId={}", creatorId);

        if (creatorId == null) {
            return 0;
        }

        return documentMapper.countByCreatorId(creatorId);
    }

    /**
     * 验证文档信息
     *
     * @param document 文档实体
     * @param isCreate 是否为创建操作
     */
    private void validateDocument(Document document, boolean isCreate) {
        if (document == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档信息不能为空");
        }

        if (isCreate) {
            if (StringUtils.isBlank(document.getTitle())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "文档标题不能为空");
            }
            if (StringUtils.isBlank(document.getType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "文档类型不能为空");
            }
            if (document.getProjectId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
            }
            if (document.getCreatorId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "创建人ID不能为空");
            }
        }

        // 验证标题长度
        if (StringUtils.isNotBlank(document.getTitle()) && document.getTitle().length() > 200) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档标题长度不能超过200");
        }
    }

    /**
     * 生成下一个版本号
     * <p>
     * 简化版本策略: 只增加修订号
     * 例如: 1.0.0 -> 1.0.1, 1.2.5 -> 1.2.6
     * </p>
     *
     * @param currentVersion 当前版本号
     * @return 新版本号
     */
    private String generateNextVersion(String currentVersion) {
        if (StringUtils.isBlank(currentVersion)) {
            return "1.0.0";
        }

        try {
            String[] parts = currentVersion.split("\\.");
            if (parts.length == 3) {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                int patch = Integer.parseInt(parts[2]);
                return major + "." + minor + "." + (patch + 1);
            }
        } catch (Exception e) {
            log.warn("解析版本号失败, currentVersion={}", currentVersion, e);
        }

        return "1.0.0";
    }
}