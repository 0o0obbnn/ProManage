package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.enums.DocumentStatus;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.utils.SecurityUtils;
import com.promanage.service.IProjectService;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.DocumentSearchRequest;
import com.promanage.service.dto.request.DocumentUploadRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.service.dto.response.DocumentDownloadInfo;
import com.promanage.service.dto.response.DocumentFolderDTO;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentFolder;
import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.Tag;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.service.service.IDocumentFolderService;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.IDocumentTagService;
import com.promanage.service.service.IDocumentViewCountService;
import com.promanage.service.service.ITagService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements IDocumentService {

    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final IDocumentFolderService documentFolderService;
    private final IProjectService projectService;
    private final IDocumentViewCountService documentViewCountService;
    private final IDocumentTagService documentTagService;
    private final ITagService tagService;

    @Override
    public Document getById(Long id, Long userId, boolean incrementView) {
        log.info("查询文档详情, id={}, userId={}, incrementView={}", id, userId, incrementView);

        // 权限检查 - 验证用户有权访问此文档
        validateDocumentAccess(id, userId);

        // 查询文档
        Document document = getByIdWithoutView(id);

        // 如果需要增加浏览次数
        if (incrementView) {
            try {
                documentViewCountService.incrementViewCount(id);
                // For immediate reflection in the response, get the latest count from cache
                Integer latestViewCount = documentViewCountService.getViewCount(id);
                if (latestViewCount != null) {
                    document.setViewCount(latestViewCount);
                }
            } catch (Exception e) {
                log.error("增加文档浏览次数失败, id={}", id, e);
                // 不影响主流程，继续返回文档
            }
        }

        return document;
    }

    public PageResult<Document> listDocuments(Long projectId, Integer page, Integer size) {
        return listDocuments(page, size, null, projectId, null, null);
    }

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

    public List<Document> searchByKeyword(String keyword) {
        log.info("搜索文档, keyword={}", keyword);

        if (StringUtils.isBlank(keyword)) {
            return List.of();
        }

        return documentMapper.searchByKeyword(keyword);
    }

    public Document getByIdWithoutView(Long id) {
        log.info("查询文档详情(不增加浏览次数), id={}", id);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        Document document = documentMapper.selectById(id);
        if (document == null) {
            log.warn("文档不存在, id={}", id);
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档不存在");
        }

        return document;
    }

    public List<DocumentFolder> getDocumentFolders(Long projectId) {
        log.info("获取文档文件夹树形结构, projectId={}", projectId);
        
        // 使用DocumentFolderService获取文件夹树形结构
        List<IDocumentFolderService.DocumentFolderTreeNode> treeNodes = documentFolderService.getFolderTree(projectId);
        
        // 转换为DocumentFolder列表
        return convertTreeNodesToFolders(treeNodes);
    }

    public void delete(Long id, Long deleterId) {
        log.info("删除文档, id={}, deleterId={}", id, deleterId);

        // 权限检查 - 验证用户有权删除此文档
        validateDocumentDeleteAccess(id, deleterId);

        // 检查文档是否存在
        getByIdWithoutView(id);

        Document update = new Document();
        update.setId(id);
        update.setDeletedBy(deleterId);
        documentMapper.updateById(update);

        // 逻辑删除文档
        documentMapper.deleteById(id);

        // 注意: 版本历史不删除,保留审计记录

        log.info("删除文档成功, id={}", id);
    }

    @Override
    public Document createDocument(Long projectId, CreateDocumentRequest request, Long creatorId) {
        log.info("创建文档, projectId={}, creatorId={}", projectId, creatorId);

        // 权限检查 - 验证用户有权在此项目中创建文档
        validateProjectCreateAccess(projectId, creatorId);

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
        document.setCreatorId(creatorId);
        Long documentId = create(document);

        syncDocumentTags(documentId, request.getTags(), projectId, creatorId);

        return getByIdWithoutView(documentId);
    }

    @Override
    public Document updateDocument(Long documentId, UpdateDocumentRequest request, Long updaterId) {
        log.info("更新文档, documentId={}, updaterId={}", documentId, updaterId);

        // 权限检查 - 验证用户有权更新此文档
        validateDocumentUpdateAccess(documentId, updaterId);

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
        document.setUpdaterId(updaterId);

        // 更新文档
        update(documentId, document, request.getChangelog());

        syncDocumentTags(documentId, request.getTags(), existingDocument.getProjectId(), updaterId);

        // 返回更新后的文档
        return getByIdWithoutView(documentId);
    }

    @Override
    public void deleteDocument(Long documentId, Long deleterId) {
        log.info("删除文档, documentId={}, deleterId={}", documentId, deleterId);

        // 权限检查 - 验证用户有权删除此文档
        validateDocumentDeleteAccess(documentId, deleterId);

        delete(documentId, deleterId);
    }

    @Override
    public PageResult<Document> listByProject(Long projectId, Integer page, Integer pageSize, Long userId) {
        log.info("查询项目文档列表, projectId={}, page={}, pageSize={}, userId={}", projectId, page, pageSize, userId);

        // 权限检查 - 验证用户有权访问此项目
        validateProjectAccess(projectId, userId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        // 构建分页对象
        Page<Document> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 20);

        // 构建查询条件
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Document::getProjectId, projectId);
        queryWrapper.orderByDesc(Document::getUpdateTime);

        // 执行查询
        IPage<Document> result = documentMapper.selectPage(pageParam, queryWrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), (int) pageParam.getCurrent(), (int) pageParam.getSize());
    }

    @Override
    public List<Document> listByProject(Long projectId, Long userId) {
        log.info("查询项目的所有文档, projectId={}, userId={}", projectId, userId);

        // 权限检查 - 验证用户有权访问此项目
        validateProjectAccess(projectId, userId);

        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }

        return documentMapper.findByProjectId(projectId);
    }

    @Override
    public List<Document> listByCreator(Long creatorId, Long userId) {
        log.info("查询用户创建的文档, creatorId={}, userId={}", creatorId, userId);

        // 权限检查 - 验证用户有权查看此创建者的文档
        validateCreatorAccess(creatorId, userId);

        if (creatorId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "创建人ID不能为空");
        }

        return documentMapper.findByCreatorId(creatorId);
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
    @PreAuthorize("hasPermission(#id, 'Document', 'document:update')")
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
        DocumentVersion newVersionRecord = new DocumentVersion();
        newVersionRecord.setDocumentId(id);
        newVersionRecord.setVersionNumber(newVersion);
        newVersionRecord.setTitle(existingDocument.getTitle());
        newVersionRecord.setContent(existingDocument.getContent());
        newVersionRecord.setContentType(existingDocument.getContentType());
        newVersionRecord.setChangeLog(StringUtils.isNotBlank(changeLog) ? changeLog : "文档更新");
        newVersionRecord.setFileUrl(existingDocument.getFileUrl());
        newVersionRecord.setFileSize(existingDocument.getFileSize());
        newVersionRecord.setCreatorId(document.getUpdaterId() != null ? document.getUpdaterId() : existingDocument.getCreatorId());
        documentVersionMapper.insert(newVersionRecord);

        log.info("更新文档成功, id={}, newVersion={}", id, newVersion);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", allEntries = true)
    public int batchDelete(List<Long> ids, Long deleterId) {
        log.info("批量删除文档, ids={}, deleterId={}", ids, deleterId);

        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }

        int count = 0;
        // 权限检查 - 验证用户有权删除这些文档
        for (Long id : ids) {
            validateDocumentDeleteAccess(id, deleterId);
            Document update = new Document();
            update.setId(id);
            update.setDeletedBy(deleterId);
            documentMapper.updateById(update);
            count += documentMapper.deleteById(id);
        }

        log.info("批量删除文档成功, count={}", count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void publish(Long id, Long updaterId) {
        log.info("发布文档, id={}, updaterId={}", id, updaterId);

        // 权限检查 - 验证用户有权发布此文档
        validateDocumentUpdateAccess(id, updaterId);

        updateStatus(id, DocumentStatus.PUBLISHED, updaterId);

        log.info("发布文档成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void archive(Long id, Long updaterId) {
        log.info("归档文档, id={}, updaterId={}", id, updaterId);

        // 权限检查 - 验证用户有权归档此文档
        validateDocumentUpdateAccess(id, updaterId);

        updateStatus(id, DocumentStatus.ARCHIVED, updaterId);

        log.info("归档文档成功, id={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documents", key = "#id")
    public void updateStatus(Long id, DocumentStatus status, Long updaterId) {
        log.info("更新文档状态, id={}, status={}, updaterId={}", id, status, updaterId);

        // 参数验证
        if (status == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效");
        }

        // 权限检查 - 验证用户有权更新此文档状态
        validateDocumentUpdateAccess(id, updaterId);

        // 检查文档是否存在
        Document document = getByIdWithoutView(id);

        // 更新状态
        document.setStatus(status.getCode());
        documentMapper.updateById(document);

        log.info("更新文档状态成功, id={}, status={}", id, status);
    }

    @Override
    @Cacheable(value = "documentVersions", key = "#documentId")
    public List<DocumentVersion> listVersions(Long documentId, Long userId) {
        log.info("查询文档版本列表, documentId={}, userId={}", documentId, userId);

        if (documentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        // 权限检查 - 验证用户有权查看此文档版本
        validateDocumentAccess(documentId, userId);

        return documentVersionMapper.findByDocumentId(documentId);
    }

    @Override
    @Cacheable(value = "documentVersions", key = "#documentId + ':' + #version")
    public DocumentVersion getVersion(Long documentId, String version, Long userId) {
        log.info("查询文档版本, documentId={}, version={}, userId={}", documentId, version, userId);

        if (documentId == null || StringUtils.isBlank(version)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID和版本号不能为空");
        }

        // 权限检查 - 验证用户有权查看此文档版本
        validateDocumentAccess(documentId, userId);

        DocumentVersion documentVersion = documentVersionMapper.findByDocumentIdAndVersion(documentId, version);
        if (documentVersion == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档版本不存在");
        }

        return documentVersion;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "documentVersions", allEntries = true)
    public Long createVersion(DocumentVersion documentVersion, Long creatorId) {
        log.info("创建文档版本, documentId={}, version={}, creatorId={}",
                documentVersion.getDocumentId(), documentVersion.getVersionNumber(), creatorId);

        // 参数验证
        if (documentVersion.getDocumentId() == null || StringUtils.isBlank(documentVersion.getVersionNumber())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID和版本号不能为空");
        }

        // 权限检查 - 验证用户有权为此文档创建版本
        validateDocumentUpdateAccess(documentVersion.getDocumentId(), creatorId);

        // 设置创建者ID
        documentVersion.setCreatorId(creatorId);

        // 保存版本
        documentVersionMapper.insert(documentVersion);

        log.info("创建文档版本成功, id={}", documentVersion.getId());
        return documentVersion.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"documents", "documentVersions"}, allEntries = true)
    public Document rollbackToVersion(Long documentId, String version, Long updaterId) {
        log.info("回滚文档到指定版本, documentId={}, version={}, updaterId={}", documentId, version, updaterId);

        // 权限检查 - 验证用户有权回滚此文档
        validateDocumentUpdateAccess(documentId, updaterId);

        // 检查文档是否存在
        Document document = getByIdWithoutView(documentId);

        // 获取指定版本
        DocumentVersion targetVersion = getVersion(documentId, version, updaterId);

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
        newVersionRecord.setCreatorId(updaterId);
        documentVersionMapper.insert(newVersionRecord);

        log.info("回滚文档成功, documentId={}, targetVersion={}, newVersion={}", documentId, version, newVersion);
        return getByIdWithoutView(documentId);
    }

    @Override
    public int countByProject(Long projectId, Long userId) {
        log.info("统计项目文档数量, projectId={}, userId={}", projectId, userId);

        // 权限检查 - 验证用户有权访问此项目
        validateProjectAccess(projectId, userId);

        if (projectId == null) {
            return 0;
        }

        return documentMapper.countByProjectId(projectId);
    }

    @Override
    public int countByCreator(Long creatorId, Long userId) {
        log.info("统计用户创建的文档数量, creatorId={}, userId={}", creatorId, userId);

        // 权限检查 - 验证用户有权查看此创建者的文档
        validateCreatorAccess(creatorId, userId);

        if (creatorId == null) {
            return 0;
        }

        return documentMapper.countByCreatorId(creatorId);
    }

    @Override
    public PageResult<Document> listAllDocuments(Integer page, Integer pageSize,
                                                 Long projectId, String status, String keyword) {
        log.info("查询所有文档列表, page={}, pageSize={}, projectId={}, status={}, keyword={}",
                page, pageSize, projectId, status, keyword);

        // 构建查询条件
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();

        // 如果指定了项目ID，按项目过滤
        if (projectId != null) {
            queryWrapper.eq(Document::getProjectId, projectId);
        }

        // 如果指定了状态，按状态过滤
        if (StringUtils.isNotBlank(status)) {
            Integer statusValue = DocumentStatus.toCode(status);
            if (statusValue != null) {
                queryWrapper.eq(Document::getStatus, statusValue);
            } else {
                log.warn("无效的状态值: {}", status);
            }
        }

        // 如果指定了关键词，在标题和摘要中搜索
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Document::getTitle, keyword)
                    .or()
                    .like(Document::getSummary, keyword)
            );
        }

        // 只查询未删除的文档（MyBatis-Plus会自动处理@TableLogic）
        // queryWrapper已经自动处理了逻辑删除，无需手动添加

        // 按更新时间倒序排列
        queryWrapper.orderByDesc(Document::getUpdateTime);

        // 分页查询
        Page<Document> pageRequest = new Page<>(page, pageSize);
        IPage<Document> pageResult = documentMapper.selectPage(pageRequest, queryWrapper);

        log.info("查询所有文档列表成功, total={}", pageResult.getTotal());

        return PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize()
        );
    }

    private PageResult<Document> searchDocuments(DocumentSearchRequest request) {
        log.info("高级搜索文档, page={}, pageSize={}, projectId={}, status={}, keyword={}, folderId={}, creatorId={}, type={}, categoryId={}, tags={}, startTime={}, endTime={}",
                request.getPage(), request.getPageSize(), request.getProjectId(), request.getStatus(), request.getKeyword(), request.getFolderId(), request.getCreatorId(), request.getType(), request.getCategoryId(), request.getTags(), request.getStartTime(), request.getEndTime());

        // 构建分页对象
        Page<Document> pageParam = new Page<>(request.getPage(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Document> queryWrapper = new LambdaQueryWrapper<>();

        // 项目ID过滤
        if (request.getProjectId() != null) {
            queryWrapper.eq(Document::getProjectId, request.getProjectId());
        }

        // 状态过滤
        if (StringUtils.isNotBlank(request.getStatus())) {
            Integer statusCode = DocumentStatus.toCode(request.getStatus());
            if (statusCode != null) {
                queryWrapper.eq(Document::getStatus, statusCode);
            } else {
                log.warn("忽略未知的文档状态: {}", request.getStatus());
            }
        }

        // 文件夹ID过滤
        if (request.getFolderId() != null) {
            queryWrapper.eq(Document::getFolderId, request.getFolderId());
        }

        // 创建人ID过滤
        if (request.getCreatorId() != null) {
            queryWrapper.eq(Document::getCreatorId, request.getCreatorId());
        }

        // 文档类型过滤
        if (StringUtils.isNotBlank(request.getType())) {
            queryWrapper.eq(Document::getType, request.getType());
        }
        if (request.getCategoryId() != null) {
            queryWrapper.eq(Document::getCategoryId, request.getCategoryId());
        }

        // 时间范围过滤
        if (request.getStartTime() != null) {
            queryWrapper.ge(Document::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            queryWrapper.le(Document::getCreateTime, request.getEndTime());
        }
        if (StringUtils.isNotBlank(request.getTags())) {
            log.info("暂不支持标签过滤, 参数已接收 tags={}", request.getTags());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Document::getTitle, request.getKeyword())
                    .or()
                    .like(Document::getContent, request.getKeyword())
                    .or()
                    .like(Document::getSummary, request.getKeyword())
            );
        }

        // 按更新时间倒序排列
        queryWrapper.orderByDesc(Document::getUpdateTime);

        // 执行查询
        IPage<Document> result = documentMapper.selectPage(pageParam, queryWrapper);

        log.info("高级搜索文档成功, total={}", result.getTotal());

        return PageResult.of(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }
    
    private List<DocumentFolder> convertTreeNodesToFolders(List<IDocumentFolderService.DocumentFolderTreeNode> treeNodes) {
        if (treeNodes == null || treeNodes.isEmpty()) {
            return List.of();
        }
        
        return treeNodes.stream().map(this::convertTreeNodeToFolder).collect(Collectors.toList());
    }
    
    private DocumentFolder convertTreeNodeToFolder(IDocumentFolderService.DocumentFolderTreeNode treeNode) {
        DocumentFolder folder = new DocumentFolder();
        folder.setId(treeNode.getId());
        folder.setName(treeNode.getName());
        folder.setDescription(treeNode.getDescription());
        folder.setProjectId(treeNode.getProjectId());
        folder.setParentId(treeNode.getParentId());
        
        // 递归处理子节点
        if (treeNode.getChildren() != null && !treeNode.getChildren().isEmpty()) {
            // 这里不设置子节点，因为DocumentFolder实体没有children字段
            // 只是为了保持树形结构的完整性
        }
        
        return folder;
    }

    @Override
    public int getWeeklyViewCount(Long documentId, Long userId) {
        log.info("获取文档周浏览量, documentId={}, userId={}", documentId, userId);

        // 权限检查 - 验证用户有权查看此文档统计
        validateDocumentAccess(documentId, userId);

        return documentViewCountService.getWeeklyViewCount(documentId);
    }

    @Override
    public Map<Long, String> getProjectNames(List<Long> projectIds, Long userId) {
        log.info("批量获取项目名称, projectIds={}, userId={}", projectIds, userId);

        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            // 解决N+1查询问题：批量查询项目信息
            List<Project> projects = projectService.listByIds(projectIds);
            
            // 权限检查：验证用户有权访问这些项目
            for (Project project : projects) {
                validateProjectAccess(project.getId(), userId);
            }
            
            // 转换为Map
            return projects.stream()
                    .collect(Collectors.toMap(
                            Project::getId,
                            Project::getName,
                            (existing, replacement) -> existing // 重复key时保留第一个
                    ));
        } catch (Exception e) {
            log.error("批量获取项目名称失败, projectIds={}", projectIds, e);
            return Collections.emptyMap();
        }
    }

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

    // ==================== 权限检查方法 ====================

    private void validateDocumentAccess(Long documentId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Document document = getByIdWithoutView(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
        }

        // 检查用户是否有权访问文档所属的项目
        validateProjectAccess(document.getProjectId(), userId);
    }

    private void validateDocumentUpdateAccess(Long documentId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Document document = getByIdWithoutView(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
        }

        // 检查用户是否有权更新文档所属的项目
        validateProjectUpdateAccess(document.getProjectId(), userId);

        // 文档创建者可以更新自己的文档
        if (!userId.equals(document.getCreatorId())) {
            // 非创建者需要项目管理员权限
            if (!hasProjectAdminPermission(document.getProjectId(), userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此文档");
            }
        }
    }

    private void validateDocumentDeleteAccess(Long documentId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Document document = getByIdWithoutView(documentId);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文档不存在");
        }

        // 检查用户是否有权删除文档所属的项目
        validateProjectDeleteAccess(document.getProjectId(), userId);

        // 文档创建者可以删除自己的文档
        if (!userId.equals(document.getCreatorId())) {
            // 非创建者需要项目管理员权限
            if (!hasProjectAdminPermission(document.getProjectId(), userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此文档");
            }
        }
    }

    private void validateProjectAccess(Long projectId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        // 检查用户是否是项目成员
        if (!isProjectMember(projectId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此项目");
        }
    }

    private void validateProjectCreateAccess(Long projectId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        // 检查用户是否是项目成员
        if (!isProjectMember(projectId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权在此项目中创建文档");
        }
    }

    private void validateProjectUpdateAccess(Long projectId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        // 检查用户是否是项目成员
        if (!isProjectMember(projectId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此项目");
        }
    }

    private void validateProjectDeleteAccess(Long projectId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Project project = projectService.getById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        // 检查用户是否是项目管理员
        if (!hasProjectAdminPermission(projectId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此项目中的文档");
        }
    }

    private void validateCreatorAccess(Long creatorId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 用户可以查看自己的文档
        if (userId.equals(creatorId)) {
            return;
        }

        // 查看他人文档需要管理员权限
        if (!hasSystemAdminPermission(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此用户的文档");
        }
    }

    private boolean isProjectMember(Long projectId, Long userId) {
        // TODO: 实现项目成员检查逻辑
        // 需要查询项目成员表，检查用户是否是项目成员
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "权限检查功能尚未实现");
    }

    private boolean hasProjectAdminPermission(Long projectId, Long userId) {
        // TODO: 实现项目管理员权限检查逻辑
        // 需要查询项目成员表，检查用户是否有管理员角色
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "权限检查功能尚未实现");
    }

    private boolean hasSystemAdminPermission(Long userId) {
        // TODO: 实现系统管理员权限检查逻辑
        // 需要查询用户角色表，检查用户是否有系统管理员角色
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "权限检查功能尚未实现");
    }

    // ==================== 新增接口方法实现 ====================

    @Override
    public PageResult<Document> searchDocuments(DocumentSearchRequest request, Long userId) {
        log.info("搜索文档, userId={}", userId);

        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        return searchDocuments(request);
    }

    @Override
    public List<DocumentFolderDTO> getFolderTree(Long projectId, Long userId) {
        log.info("获取文档文件夹树形结构, projectId={}, userId={}", projectId, userId);

        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 权限检查
        if (projectId != null) {
            validateProjectAccess(projectId, userId);
        }

        // TODO: 实现文件夹树形结构获取逻辑
        // 需要实现DocumentFolderDTO和相关的转换逻辑
        return List.of();
    }

    @Override
    public Document upload(DocumentUploadRequest request, Long uploaderId) throws IOException {
        log.info("上传文档, uploaderId={}", uploaderId);

        if (uploaderId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 权限检查
        validateProjectCreateAccess(request.getProjectId(), uploaderId);

        // TODO: 实现文档上传逻辑
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "文档上传功能尚未实现");
    }

    @Override
    public DocumentDownloadInfo getDownloadInfo(Long id, Long userId) {
        log.info("获取文档下载信息, id={}, userId={}", id, userId);

        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 权限检查
        validateDocumentAccess(id, userId);

        // TODO: 实现文档下载信息获取逻辑
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "文档下载功能尚未实现");
    }

    @Override
    public void downloadDocument(Long id, Long userId, HttpServletResponse response) throws IOException {
        log.info("下载文档, id={}, userId={}", id, userId);

        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        // 权限检查
        validateDocumentAccess(id, userId);

        // TODO: 实现文档下载逻辑
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "文档下载功能尚未实现");
    }

    // ==================== 标签管理方法 ====================

    private void syncDocumentTags(Long documentId, List<String> tagNames, Long projectId, Long userId) {
        if (documentId == null) {
            return;
        }

        if (CollectionUtils.isEmpty(tagNames)) {
            documentTagService.setDocumentTags(documentId, List.of());
            return;
        }

        List<String> normalizedNames = tagNames.stream()
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (normalizedNames.isEmpty()) {
            documentTagService.setDocumentTags(documentId, List.of());
            return;
        }

        List<Tag> tags = tagService.ensureTagsExist(normalizedNames);
        List<Long> tagIds = tags.stream()
                .map(Tag::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        documentTagService.setDocumentTags(documentId, tagIds);
        log.info("同步文档标签成功, documentId={}, tagCount={}", documentId, tagIds.size());
    }

    // ==================== 实现缺失的接口方法 ====================

    @Override
    public void delete(Long id) {
        delete(id, SecurityUtils.getCurrentUserId().orElse(null));
    }

    @Override
    public void toggleFavorite(Long documentId, Long userId, boolean favorite) {
        log.info("切换文档收藏状态, documentId={}, userId={}, favorite={}", documentId, userId, favorite);
        // TODO: 实现收藏功能
        throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "收藏功能尚未实现");
    }

    @Override
    public boolean isFavorited(Long documentId, Long userId) {
        log.info("检查文档收藏状态, documentId={}, userId={}", documentId, userId);
        // TODO: 实现收藏状态检查
        return false;
    }

    @Override
    public int getFavoriteCount(Long documentId, Long userId) {
        log.info("获取文档收藏数量, documentId={}, userId={}", documentId, userId);
        // TODO: 实现收藏数量统计
        return 0;
    }

}