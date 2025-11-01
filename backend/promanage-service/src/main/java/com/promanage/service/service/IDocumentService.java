package com.promanage.service.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.promanage.common.enums.DocumentStatus;
import com.promanage.common.result.PageResult;
import com.promanage.service.dto.request.CreateDocumentRequest;
import com.promanage.service.dto.request.DocumentSearchRequest;
import com.promanage.service.dto.request.DocumentUploadRequest;
import com.promanage.service.dto.request.UpdateDocumentRequest;
import com.promanage.service.dto.response.DocumentDTO;
import com.promanage.service.dto.response.DocumentDownloadInfo;
import com.promanage.service.dto.response.DocumentFolderDTO;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.DocumentVersion;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文档服务接口
 *
 * <p>提供文档管理的业务逻辑,包括文档的CRUD操作、版本管理和搜索功能
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface IDocumentService {
  /**
   * 根据ID查询文档详情
   *
   * @param id 文档ID
   * @param userId 用户ID
   * @param incrementView 是否增加浏览次数
   * @return 文档实体
   */
  Document getById(Long id, Long userId, boolean incrementView);

  /**
   * 根据ID查询文档详情(默认增加浏览次数)
   *
   * @param id 文档ID
   * @param userId 用户ID
   * @return 文档实体
   */
  default Document getById(Long id, Long userId) {
    return getById(id, userId, true);
  }

  /**
   * 获取项目文档列表
   *
   * @param projectId 项目ID
   * @param page 页码
   * @param pageSize 每页大小
   * @param userId 用户ID
   * @return 分页结果
   */
  PageResult<Document> listByProject(Long projectId, Integer page, Integer pageSize, 
                                    String keyword, String status, String type, Long userId);

  /**
   * 查询项目的所有文档
   *
   * @param projectId 项目ID
   * @param userId 用户ID
   * @return 文档列表
   */
  List<Document> listByProject(Long projectId, Long userId);

  /**
   * 查询用户创建的文档
   *
   * @param creatorId 创建人ID
   * @param userId 用户ID
   * @return 文档列表
   */
  List<Document> listByCreator(Long creatorId, Long userId);

  /**
   * 根据项目ID和创建文档请求创建文档
   *
   * @param projectId 项目ID
   * @param request 创建文档请求
   * @param creatorId 创建人ID
   * @return 文档实体
   */
  Document createDocument(Long projectId, CreateDocumentRequest request, Long creatorId);

  /**
   * 根据文档ID和更新文档请求更新文档
   *
   * @param documentId 文档ID
   * @param request 更新文档请求
   * @param updaterId 更新人ID
   * @return 文档实体
   */
  Document updateDocument(Long documentId, UpdateDocumentRequest request, Long updaterId);

  /**
   * 根据文档ID删除文档
   *
   * @param documentId 文档ID
   * @param deleterId 删除人ID
   */
  void deleteDocument(Long documentId, Long deleterId);

  /**
   * 创建文档
   *
   * <p>会自动创建版本1.0.0
   *
   * @param document 文档实体
   * @param creatorId 创建人ID
   * @return 文档ID
   */
  Long create(Document document);

  /**
   * 更新文档
   *
   * <p>会自动创建新版本
   *
   * @param id 文档ID
   * @param document 文档实体
   * @param changeLog 变更日志
   * @param updaterId 更新人ID
   */
  void update(Long id, Document document, String changeLog);

  /**
   * 删除文档
   *
   * @param id 文档ID
   * @param deleterId 删除人ID
   */
  void delete(Long id);

  /**
   * 批量删除文档
   *
   * @param ids 文档ID列表
   * @param deleterId 删除人ID
   * @return 删除的记录数
   */
  int batchDelete(List<Long> ids, Long deleterId);

  /**
   * 更新文档状态
   *
   * @param id 文档ID
   * @param status 状态枚举
   * @param updaterId 更新人ID
   */
  void updateStatus(Long id, DocumentStatus status, Long updaterId);

  /**
   * 发布文档
   *
   * @param id 文档ID
   * @param updaterId 更新人ID
   */
  default void publish(Long id, Long updaterId) {
    updateStatus(id, DocumentStatus.PUBLISHED, updaterId);
  }

  /**
   * 归档文档
   *
   * @param id 文档ID
   * @param updaterId 更新人ID
   */
  default void archive(Long id, Long updaterId) {
    updateStatus(id, DocumentStatus.ARCHIVED, updaterId);
  }

  /**
   * 查询文档的所有版本
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 版本列表
   */
  List<DocumentVersion> listVersions(Long documentId, Long userId);

  /**
   * 根据版本号查询文档版本
   *
   * @param documentId 文档ID
   * @param version 版本号
   * @param userId 用户ID
   * @return 文档版本实体
   */
  DocumentVersion getVersion(Long documentId, String version, Long userId);

  /**
   * 创建文档版本
   *
   * @param documentVersion 文档版本实体
   * @param creatorId 创建人ID
   * @return 版本ID
   */
  Long createVersion(DocumentVersion documentVersion, Long creatorId);

  /**
   * 回滚到指定版本
   *
   * <p>将指定版本的内容恢复为当前版本
   *
   * @param documentId 文档ID
   * @param version 版本号
   * @param updaterId 更新人ID
   */
  Document rollbackToVersion(Long documentId, String version, Long updaterId);

  /**
   * 统计项目文档数量
   *
   * @param projectId 项目ID
   * @param userId 用户ID
   * @return 文档数量
   */
  int countByProject(Long projectId, Long userId);

  /**
   * 统计用户创建的文档数量
   *
   * @param creatorId 创建人ID
   * @param userId 用户ID
   * @return 文档数量
   */
  int countByCreator(Long creatorId, Long userId);

  /**
   * 查询当前用户可访问的所有文档
   *
   * @param page 页码
   * @param pageSize 每页大小
   * @param projectId 项目ID（可选）
   * @param status 文档状态（可选）
   * @param keyword 搜索关键词（可选）
   * @return 分页结果
   */
  PageResult<Document> listAllDocuments(
      Integer page, Integer pageSize, Long projectId, String status, String keyword);

  /**
   * 搜索文档
   *
   * <p>支持多种过滤条件的文档搜索
   *
   * @param request 搜索请求参数
   * @param userId 用户ID
   * @return 分页结果
   */
  PageResult<Document> searchDocuments(DocumentSearchRequest request, Long userId);

  /**
   * 获取文档文件夹树形结构
   *
   * @param projectId 项目ID（可选，为null时返回所有项目的文件夹）
   * @param userId 用户ID
   * @return 文件夹树形结构列表
   */
  List<DocumentFolderDTO> getFolderTree(Long projectId, Long userId);

  /**
   * 统计文档的周浏览量(最近7天)
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 周浏览量
   */
  int getWeeklyViewCount(Long documentId, Long userId);

  /**
   * 收藏/取消收藏文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @param favorite true-收藏, false-取消收藏
   */
  void toggleFavorite(Long documentId, Long userId, boolean favorite);

  /**
   * 收藏文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   */
  default void favorite(Long documentId, Long userId) {
    toggleFavorite(documentId, userId, true);
  }

  /**
   * 取消收藏文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   */
  default void unfavorite(Long documentId, Long userId) {
    toggleFavorite(documentId, userId, false);
  }

  /**
   * 获取文档收藏数量
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 收藏数量
   */
  int getFavoriteCount(Long documentId, Long userId);

  /**
   * 检查用户是否收藏了文档
   *
   * @param documentId 文档ID
   * @param userId 用户ID
   * @return 是否已收藏
   */
  boolean isFavorited(Long documentId, Long userId);

  /**
   * 根据项目ID列表获取项目名称映射
   *
   * @param projectIds 项目ID列表
   * @param userId 用户ID
   * @return 项目ID到项目名称的映射
   */
  Map<Long, String> getProjectNames(List<Long> projectIds, Long userId);

  /**
   * Handles the upload of a new document or a new version of an existing document.
   *
   * @param projectId The ID of the project the document belongs to.
   * @param uploaderId The ID of the user uploading the document.
   * @param metadata The metadata associated with the upload.
   * @param file The file being uploaded.
   * @return A DTO representing the created or updated document.
   */
  DocumentDTO uploadDocument(
      Long projectId, Long uploaderId, DocumentUploadRequest metadata, MultipartFile file);

  Document upload(DocumentUploadRequest request, Long uploaderId) throws IOException;

  /**
   * 获取文档下载信息
   *
   * @param id 文档ID
   * @param userId 用户ID
   * @return 下载信息
   */
  DocumentDownloadInfo getDownloadInfo(Long id, Long userId);

  /**
   * 下载文档(兼容旧接口)
   *
   * @param id 文档ID
   * @param userId 用户ID
   * @param response HTTP响应
   * @throws IOException IO异常
   * @deprecated 使用 {@link #getDownloadInfo(Long, Long)} 替代
   */
  @Deprecated
  void downloadDocument(Long id, Long userId, HttpServletResponse response) throws IOException;
}
