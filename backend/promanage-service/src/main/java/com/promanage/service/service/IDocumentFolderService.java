package com.promanage.service.service;

import com.promanage.common.result.PageResult;
import com.promanage.service.entity.DocumentFolder;

import java.util.List;
import java.util.Map;

/**
 * 文档文件夹服务接口
 * <p>
 * 提供文档文件夹管理的业务逻辑
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-04
 */
public interface IDocumentFolderService {

    /**
     * 创建文件夹
     *
     * @param folder 文件夹实体
     * @return 文件夹ID
     */
    Long createFolder(DocumentFolder folder);

    /**
     * 根据ID查询文件夹详情
     *
     * @param id 文件夹ID
     * @return 文件夹实体
     */
    DocumentFolder getFolderById(Long id);

    /**
     * 更新文件夹
     *
     * @param id     文件夹ID
     * @param folder 文件夹实体
     */
    void updateFolder(Long id, DocumentFolder folder);

    /**
     * 删除文件夹
     *
     * @param id 文件夹ID
     */
    void deleteFolder(Long id);

    /**
     * 查询项目的所有文件夹
     *
     * @param projectId 项目ID
     * @return 文件夹列表
     */
    List<DocumentFolder> listByProjectId(Long projectId);

    /**
     * 根据父文件夹ID查找子文件夹列表
     *
     * @param parentId 父文件夹ID
     * @return 子文件夹列表
     */
    List<DocumentFolder> findByParentId(Long parentId);

    /**
     * 根据文件夹ID列表获取文件夹名称映射
     *
     * @param folderIds 文件夹ID列表
     * @return 文件夹ID到文件夹名称的映射
     */
    Map<Long, String> getFolderNamesByIds(List<Long> folderIds);

    /**
     * 查询项目中指定父文件夹下的所有子文件夹
     *
     * @param projectId 项目ID
     * @param parentId  父文件夹ID
     * @return 文件夹列表
     */
    List<DocumentFolder> listByProjectIdAndParentId(Long projectId, Long parentId);

    /**
     * 获取文件夹树形结构
     *
     * @param projectId 项目ID
     * @return 文件夹树形结构列表
     */
    List<DocumentFolderTreeNode> getFolderTree(Long projectId);

    /**
     * 分页查询文件夹列表
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param projectId 项目ID（可选）
     * @param parentId  父文件夹ID（可选）
     * @return 分页结果
     */
    PageResult<DocumentFolder> listFolders(Integer page, Integer pageSize,
                                          Long projectId, Long parentId);

    /**
     * 文件夹树节点DTO
     */
    class DocumentFolderTreeNode {
        private Long id;
        private String name;
        private String description;
        private Long projectId;
        private Long parentId;
        private Integer documentCount;
        private List<DocumentFolderTreeNode> children;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public Integer getDocumentCount() { return documentCount; }
        public void setDocumentCount(Integer documentCount) { this.documentCount = documentCount; }
        public List<DocumentFolderTreeNode> getChildren() { return children; }
        public void setChildren(List<DocumentFolderTreeNode> children) { this.children = children; }
    }
}
