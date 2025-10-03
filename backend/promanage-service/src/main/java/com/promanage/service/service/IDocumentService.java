package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;

import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;

import java.util.List;

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
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @param projectId 项目ID (可选)
     * @param type 文档类型 (可选)
     * @param status 文档状态 (可选)
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
     * @param id 文档ID
     * @param document 文档实体
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
     * @param id 文档ID
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
     * @param version 版本号
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
     * @param version 版本号
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
}