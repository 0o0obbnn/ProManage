package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;

import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;

import java.util.List;
import java.util.Map;

/**
 * 文档服务接口
 * <p>
 * 提供文档管理的业务逻辑,包括文档的CRUD操作、版本管理和搜索功能
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface IDocumentService {

    /**
     * 根据ID查询文档详情
     * <p>
     * 会自动增加浏览次数
     * </p>
     *
     * @param id 文档ID
     * @return 文档实体
     */
    Document getById(Long id);

    /**
     * 根据ID查询文档详情 (不增加浏览次数)
     *
     * @param id 文档ID
     * @return 文档实体
     */
    Document getByIdWithoutView(Long id);

    /**
     * 分页查询文档列表
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param keyword   搜索关键词
     * @param projectId 项目ID (可选)
     * @param type      文档类型 (可选)
     * @param status    文档状态 (可选)
     * @return 分页结果
     */
    PageResult<Document> listDocuments(Integer page, Integer pageSize, String keyword,
                                       Long projectId, String type, Integer status);

    /**
     * 获取项目文档列表
     *
     * @param projectId 项目ID
     * @param page      页码
     * @param size      每页大小
     * @return 分页结果
     */
    PageResult<Document> listDocuments(Long projectId, Integer page, Integer size);

    /**
     * 根据项目ID和创建文档请求创建文档
     *
     * @param projectId 项目ID
     * @param request   创建文档请求
     * @return 文档实体
     */
    Document createDocument(Long projectId, CreateDocumentRequest request);

    /**
     * 根据文档ID获取文档详情
     *
     * @param documentId 文档ID
     * @return 文档实体
     */
    Document getDocumentById(Long documentId);

    /**
     * 根据文档ID和更新文档请求更新文档
     *
     * @param documentId 文档ID
     * @param request    更新文档请求
     * @return 文档实体
     */
    Document updateDocument(Long documentId, UpdateDocumentRequest request);

    /**
     * 根据文档ID删除文档
     *
     * @param documentId 文档ID
     */
    void deleteDocument(Long documentId);

    /**
     * 查询项目的所有文档
     *
     * @param projectId 项目ID
     * @return 文档列表
     */
    List<Document> listByProjectId(Long projectId);

    /**
     * 查询用户创建的文档
     *
     * @param creatorId 创建人ID
     * @return 文档列表
     */
    List<Document> listByCreatorId(Long creatorId);

    /**
     * 搜索文档
     * <p>
     * 在标题、内容和摘要中搜索关键词
     * </p>
     *
     * @param keyword 搜索关键词
     * @return 文档列表
     */
    List<Document> searchByKeyword(String keyword);

    /**
     * 创建文档
     * <p>
     * 会自动创建版本1.0.0
     * </p>
     *
     * @param document 文档实体
     * @return 文档ID
     */
    Long create(Document document);

    /**
     * 更新文档
     * <p>
     * 会自动创建新版本
     * </p>
     *
     * @param id        文档ID
     * @param document  文档实体
     * @param changeLog 变更日志
     */
    void update(Long id, Document document, String changeLog);

    /**
     * 删除文档
     *
     * @param id 文档ID
     */
    void delete(Long id);

    /**
     * 批量删除文档
     *
     * @param ids 文档ID列表
     * @return 删除的记录数
     */
    int batchDelete(List<Long> ids);

    /**
     * 发布文档
     * <p>
     * 将文档状态从草稿改为已发布
     * </p>
     *
     * @param id 文档ID
     */
    void publish(Long id);

    /**
     * 归档文档
     * <p>
     * 将文档状态改为已归档
     * </p>
     *
     * @param id 文档ID
     */
    void archive(Long id);

    /**
     * 更新文档状态
     *
     * @param id     文档ID
     * @param status 状态值
     */
    void updateStatus(Long id, Integer status);

    /**
     * 查询文档的所有版本
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    List<DocumentVersion> listVersions(Long documentId);

    /**
     * 根据版本号查询文档版本
     *
     * @param documentId 文档ID
     * @param version    版本号
     * @return 文档版本实体
     */
    DocumentVersion getVersion(Long documentId, String version);

    /**
     * 创建文档版本
     *
     * @param documentVersion 文档版本实体
     * @return 版本ID
     */
    Long createVersion(DocumentVersion documentVersion);

    /**
     * 回滚到指定版本
     * <p>
     * 将指定版本的内容恢复为当前版本
     * </p>
     *
     * @param documentId 文档ID
     * @param version    版本号
     */
    void rollbackToVersion(Long documentId, String version);

    /**
     * 统计项目文档数量
     *
     * @param projectId 项目ID
     * @return 文档数量
     */
    int countByProjectId(Long projectId);

    /**
     * 统计用户创建的文档数量
     *
     * @param creatorId 创建人ID
     * @return 文档数量
     */
    int countByCreatorId(Long creatorId);

    /**
     * 查询当前用户可访问的所有文档（跨项目）
     * <p>
     * 根据用户权限过滤文档，支持多种过滤条件和搜索
     * </p>
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param projectId 项目ID（可选，为null时查询所有项目）
     * @param status    文档状态（可选）
     * @param keyword   搜索关键词（可选，在标题和内容中搜索）
     * @return 分页结果
     */
    PageResult<Document> listAllDocuments(Integer page, Integer pageSize,
                                         Long projectId, String status, String keyword);

    /**
     * 高级搜索文档
     * <p>
     * 支持多种过滤条件的文档搜索
     * </p>
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param projectId 项目ID（可选）
     * @param status    文档状态（可选）
     * @param keyword   搜索关键词（可选）
     * @param folderId  文件夹ID（可选）
     * @param creatorId 创建人ID（可选）
     * @param type      文档类型（可选）
     * @param startTime 创建时间开始（可选）
     * @param endTime   创建时间结束（可选）
     * @return 分页结果
     */
    PageResult<Document> searchDocuments(Integer page, Integer pageSize,
                                        Long projectId, Integer status, String keyword,
                                        Long folderId, Long creatorId, String type,
                                        java.time.LocalDateTime startTime,
                                        java.time.LocalDateTime endTime);

    /**
     * 重载方法，保持向后兼容
     */
    PageResult<Document> searchDocuments(Integer page, Integer pageSize,
                                        Long projectId, Integer status, String keyword,
                                        Long folderId, Long creatorId, String type);

    /**
     * 获取文档文件夹树形结构
     * <p>
     * 返回文档的文件夹组织结构，支持按项目过滤
     * </p>
     *
     * @param projectId 项目ID（可选，为null时返回所有项目的文件夹）
     * @return 文件夹树形结构列表
     */
    List<DocumentFolder> getDocumentFolders(Long projectId);

    /**
     * 统计文档的周浏览量
     * <p>
     * 统计最近7天的浏览次数
     * </p>
     *
     * @param documentId 文档ID
     * @return 周浏览量
     */
    int getWeekViewCount(Long documentId);

    /**
     * 收藏文档
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     */
    void favoriteDocument(Long documentId, Long userId);

    /**
     * 取消收藏文档
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     */
    void unfavoriteDocument(Long documentId, Long userId);

    /**
     * 获取文档收藏数量
     *
     * @param documentId 文档ID
     * @return 收藏数量
     */
    int getFavoriteCount(Long documentId);

    /**
     * 检查用户是否收藏了文档
     *
     * @param documentId 文档ID
     * @param userId     用户ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long documentId, Long userId);

    /**
     * 根据项目ID列表获取项目名称映射
     *
     * @param projectIds 项目ID列表
     * @return 项目ID到项目名称的映射
     */
    Map<Long, String> getProjectNamesByIds(List<Long> projectIds);

    /**
     * 文档文件夹DTO
     */
    class DocumentFolder {
        private Long id;
        private String name;
        private String description;
        private Long parentId;
        private Long projectId;
        private Integer documentCount;
        private List<DocumentFolder> children;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Integer getDocumentCount() { return documentCount; }
        public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }
        public List<DocumentFolder> getChildren() { return children; }
        public void setChildren(List<DocumentFolder> children) { this.children = children; }
    }
}