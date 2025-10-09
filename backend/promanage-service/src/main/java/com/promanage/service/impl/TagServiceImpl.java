package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.entity.DocumentTag;
import com.promanage.service.entity.Tag;
import com.promanage.service.mapper.DocumentTagMapper;
import com.promanage.service.mapper.TagMapper;
import com.promanage.service.service.ITagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 * <p>
 * 实现标签管理的所有业务逻辑
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    private final TagMapper tagMapper;
    private final DocumentTagMapper documentTagMapper;
    // Backward-compatible helper methods (not part of current ITagService)
    public Tag getById(Long tagId) {
        return tagId == null ? null : super.getById(tagId);
    }

    public Tag getByName(String name) {
        if (StringUtils.isBlank(name)) return null;
        return tagMapper.findByName(name, null);
    }

    public List<Tag> listAll() {
        return this.list();
    }

    public void updateTag(Tag tag) {
        if (tag == null || tag.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签ID不能为空");
        }
        tag.setUpdatedAt(LocalDateTime.now());
        updateById(tag);
    }

    @Override
    public List<Tag> ensureTagsExist(List<String> tagNames) {
        List<Tag> result = new ArrayList<>();
        if (tagNames == null || tagNames.isEmpty()) {
            return result;
        }
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        for (String name : tagNames) {
            if (StringUtils.isBlank(name)) continue;
            Tag existing = tagMapper.findByName(name, null);
            if (existing != null) {
                result.add(existing);
            } else {
                Tag newTag = new Tag();
                newTag.setName(name.trim());
                newTag.setIsActive(true);
                newTag.setUsageCount(0);
                newTag.setCreatorId(currentUserId);
                newTag.setCreatedAt(LocalDateTime.now());
                newTag.setUpdatedAt(LocalDateTime.now());
                save(newTag);
                result.add(newTag);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(Tag tag) {
        log.info("创建标签: name={}, projectId={}", tag.getName(), tag.getProjectId());

        // 参数验证
        validateTag(tag, true);

        // 检查标签名称是否已存在
        Tag existingTag = tagMapper.findByName(tag.getName(), tag.getProjectId());
        if (existingTag != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签名称已存在");
        }

        // 设置默认值
        if (tag.getUsageCount() == null) {
            tag.setUsageCount(0);
        }
        if (tag.getIsActive() == null) {
            tag.setIsActive(true);
        }
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());

        // 保存标签
        save(tag);

        log.info("创建标签成功, id={}, name={}", tag.getId(), tag.getName());
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long id, Tag tag) {
        log.info("更新标签: id={}", id);

        // 检查标签是否存在
        Tag existingTag = getById(id);
        if (existingTag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }

        // 检查标签名称是否已存在（排除当前标签）
        if (StringUtils.isNotBlank(tag.getName()) && !tag.getName().equals(existingTag.getName())) {
            Tag duplicateTag = tagMapper.findByName(tag.getName(), existingTag.getProjectId());
            if (duplicateTag != null && !duplicateTag.getId().equals(id)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "标签名称已存在");
            }
        }

        // 更新字段
        if (StringUtils.isNotBlank(tag.getName())) {
            existingTag.setName(tag.getName());
        }
        if (StringUtils.isNotBlank(tag.getColor())) {
            existingTag.setColor(tag.getColor());
        }
        if (StringUtils.isNotBlank(tag.getDescription())) {
            existingTag.setDescription(tag.getDescription());
        }
        if (tag.getIsActive() != null) {
            existingTag.setIsActive(tag.getIsActive());
        }

        existingTag.setUpdatedAt(LocalDateTime.now());

        // 保存更新
        updateById(existingTag);

        log.info("更新标签成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        log.info("删除标签: id={}", id);

        // 检查标签是否存在
        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }

        // 删除文档标签关联
        documentTagMapper.deleteByTagId(id);

        // 删除标签
        removeById(id);

        log.info("删除标签成功, id={}", id);
    }

    @Override
    public Tag getTagById(Long id) {
        log.info("查询标签详情: id={}", id);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签ID不能为空");
        }

        Tag tag = getById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }

        return tag;
    }

    @Override
    public List<Tag> getTagsByProjectId(Long projectId) {
        log.info("查询项目标签列表: projectId={}", projectId);

        return tagMapper.findByProjectId(projectId);
    }

    @Override
    public List<Tag> getTagsByDocumentId(Long documentId) {
        log.info("查询文档关联的标签列表: documentId={}", documentId);

        if (documentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        return tagMapper.findByDocumentId(documentId);
    }

    @Override
    public Tag getTagByName(String name, Long projectId) {
        log.info("根据名称查询标签: name={}, projectId={}", name, projectId);

        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签名称不能为空");
        }

        return tagMapper.findByName(name, projectId);
    }

    @Override
    public List<Tag> getPopularTags(Long projectId, Integer limit) {
        log.info("查询热门标签: projectId={}, limit={}", projectId, limit);

        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10个
        }

        return tagMapper.findPopularTags(projectId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToDocument(Long documentId, List<Long> tagIds) {
        log.info("为文档添加标签: documentId={}, tagIds={}", documentId, tagIds);

        if (documentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 验证标签是否存在
        for (Long tagId : tagIds) {
            Tag tag = getById(tagId);
            if (tag == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在: " + tagId);
            }

            // 检查是否已关联
            int count = documentTagMapper.countByDocumentIdAndTagId(documentId, tagId);
            if (count > 0) {
                continue; // 已关联，跳过
            }

            // 创建关联
            DocumentTag documentTag = new DocumentTag();
            documentTag.setDocumentId(documentId);
            documentTag.setTagId(tagId);
            documentTag.setCreatedAt(LocalDateTime.now());
            
            // 从安全上下文获取当前用户ID
            Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
            documentTag.setCreatorId(currentUserId);

            documentTagMapper.insert(documentTag);

            // 增加标签使用次数
            tagMapper.incrementUsageCount(tagId);
        }

        log.info("为文档添加标签成功: documentId={}, tagIds={}", documentId, tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTagsFromDocument(Long documentId, List<Long> tagIds) {
        log.info("从文档中移除标签: documentId={}, tagIds={}", documentId, tagIds);

        if (documentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        for (Long tagId : tagIds) {
            // 检查是否已关联
            int count = documentTagMapper.countByDocumentIdAndTagId(documentId, tagId);
            if (count == 0) {
                continue; // 未关联，跳过
            }

            // 删除关联
            LambdaQueryWrapper<DocumentTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DocumentTag::getDocumentId, documentId);
            wrapper.eq(DocumentTag::getTagId, tagId);
            documentTagMapper.delete(wrapper);

            // 减少标签使用次数
            tagMapper.decrementUsageCount(tagId);
        }

        log.info("从文档中移除标签成功: documentId={}, tagIds={}", documentId, tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDocumentTags(Long documentId, List<Long> tagIds) {
        log.info("更新文档标签: documentId={}, tagIds={}", documentId, tagIds);

        if (documentId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文档ID不能为空");
        }

        // 获取当前关联的标签ID列表
        List<Long> currentTagIds = documentTagMapper.findTagIdsByDocumentId(documentId);

        // 计算需要添加和删除的标签
        List<Long> toAdd = tagIds != null ? 
                tagIds.stream().filter(id -> !currentTagIds.contains(id)).collect(Collectors.toList()) : 
                new ArrayList<>();
        
        List<Long> toRemove = currentTagIds != null ? 
                currentTagIds.stream().filter(id -> tagIds == null || !tagIds.contains(id)).collect(Collectors.toList()) : 
                new ArrayList<>();

        // 添加新标签
        if (!toAdd.isEmpty()) {
            addTagsToDocument(documentId, toAdd);
        }

        // 移除旧标签
        if (!toRemove.isEmpty()) {
            removeTagsFromDocument(documentId, toRemove);
        }

        log.info("更新文档标签成功: documentId={}, tagIds={}", documentId, tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag getOrCreateTag(String name, Long projectId, Long creatorId) {
        log.info("获取或创建标签: name={}, projectId={}", name, projectId);

        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签名称不能为空");
        }

        // 尝试获取现有标签
        Tag existingTag = tagMapper.findByName(name, projectId);
        if (existingTag != null) {
            return existingTag;
        }

        // 创建新标签
        Tag tag = new Tag();
        tag.setName(name);
        tag.setProjectId(projectId);
        tag.setCreatorId(creatorId);
        tag.setUsageCount(0);
        tag.setIsActive(true);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());

        save(tag);

        log.info("创建新标签成功: id={}, name={}", tag.getId(), tag.getName());
        return tag;
    }

    /**
     * 验证标签信息
     *
     * @param tag      标签实体
     * @param isCreate 是否为创建操作
     */
    private void validateTag(Tag tag, boolean isCreate) {
        if (tag == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签信息不能为空");
        }

        if (isCreate) {
            if (StringUtils.isBlank(tag.getName())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "标签名称不能为空");
            }
            if (tag.getCreatorId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "创建人ID不能为空");
            }
        }

        // 验证名称长度
        if (StringUtils.isNotBlank(tag.getName()) && tag.getName().length() > 50) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签名称长度不能超过50");
        }

        // 验证颜色格式
        if (StringUtils.isNotBlank(tag.getColor()) && !tag.getColor().matches("^#[0-9A-Fa-f]{6}$")) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签颜色格式不正确，应为十六进制颜色代码（如：#1890ff）");
        }

        // 验证描述长度
        if (StringUtils.isNotBlank(tag.getDescription()) && tag.getDescription().length() > 200) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标签描述长度不能超过200");
        }
    }
}