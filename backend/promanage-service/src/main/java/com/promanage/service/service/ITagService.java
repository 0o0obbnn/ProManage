package com.promanage.service.service;

import com.promanage.service.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 * <p>
 * 提供标签管理的业务逻辑
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
public interface ITagService {

    /**
     * 创建标签
     *
     * @param tag 标签实体
     * @return 标签ID
     */
    Long createTag(Tag tag);

    /**
     * 更新标签
     *
     * @param id  标签ID
     * @param tag 标签实体
     */
    void updateTag(Long id, Tag tag);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);

    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签实体
     */
    Tag getTagById(Long id);

    /**
     * 根据项目ID获取标签列表
     *
     * @param projectId 项目ID（null表示全局标签）
     * @return 标签列表
     */
    List<Tag> getTagsByProjectId(Long projectId);

    /**
     * 根据文档ID获取关联的标签列表
     *
     * @param documentId 文档ID
     * @return 标签列表
     */
    List<Tag> getTagsByDocumentId(Long documentId);

    /**
     * 根据名称查找标签
     *
     * @param name     标签名称
     * @param projectId 项目ID（null表示全局标签）
     * @return 标签实体
     */
    Tag getTagByName(String name, Long projectId);

    /**
     * 获取热门标签
     *
     * @param projectId 项目ID（null表示全局标签）
     * @param limit     限制数量
     * @return 标签列表
     */
    List<Tag> getPopularTags(Long projectId, Integer limit);

    /**
     * 为文档添加标签
     *
     * @param documentId 文档ID
     * @param tagIds     标签ID列表
     */
    void addTagsToDocument(Long documentId, List<Long> tagIds);

    /**
     * 从文档中移除标签
     *
     * @param documentId 文档ID
     * @param tagIds     标签ID列表
     */
    void removeTagsFromDocument(Long documentId, List<Long> tagIds);

    /**
     * 更新文档标签
     *
     * @param documentId 文档ID
     * @param tagIds     新的标签ID列表
     */
    void updateDocumentTags(Long documentId, List<Long> tagIds);

    /**
     * 获取或创建标签（如果不存在则创建）
     *
     * @param name      标签名称
     * @param projectId 项目ID（null表示全局标签）
     * @param creatorId 创建人ID
     * @return 标签实体
     */
    Tag getOrCreateTag(String name, Long projectId, Long creatorId);

    /**
     * 批量确保标签存在（按名称），返回对应的标签列表
     */
    List<Tag> ensureTagsExist(List<String> tagNames);
}