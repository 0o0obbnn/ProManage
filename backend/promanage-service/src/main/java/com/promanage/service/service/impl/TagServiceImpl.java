package com.promanage.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.exception.BusinessException;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements ITagService {

    private final TagMapper tagMapper;
    private final DocumentTagMapper documentTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTag(Tag tag) {
        if (tag == null || StringUtils.isBlank(tag.getName())) {
            throw new BusinessException("标签名称不能为空");
        }
        Tag existing = tagMapper.findByName(tag.getName(), null);
        if (existing != null) {
            return existing.getId();
        }
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        tag.setDeleted(false);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTag(Long id, Tag tag) {
        if (id == null) {
            throw new BusinessException("标签ID不能为空");
        }
        Tag toUpdate = new Tag();
        toUpdate.setId(id);
        toUpdate.setName(tag.getName());
        toUpdate.setColor(tag.getColor());
        toUpdate.setUpdateTime(LocalDateTime.now());
        tagMapper.updateById(toUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long tagId) {
        if (tagId == null) return;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setDeleted(true);
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.updateById(tag);
    }

    @Override
    public Tag getTagById(Long tagId) {
        return tagId == null ? null : tagMapper.selectById(tagId);
    }

    @Override
    public List<Tag> getTagsByProjectId(Long projectId) {
        return tagMapper.findByProjectId(projectId);
    }

    @Override
    public List<Tag> getTagsByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        return tagMapper.findByDocumentId(documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag getTagByName(String name, Long projectId) {
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("标签名称不能为空");
        }
        return tagMapper.findByName(name, projectId);
    }

    @Override
    public List<Tag> getPopularTags(Long projectId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return tagMapper.findPopularTags(projectId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTagsToDocument(Long documentId, List<Long> tagIds) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            if (tagId == null) continue;
            int count = documentTagMapper.countByDocumentIdAndTagId(documentId, tagId);
            if (count > 0) continue;
            DocumentTag dt = new DocumentTag();
            dt.setDocumentId(documentId);
            dt.setTagId(tagId);
            dt.setCreatedAt(LocalDateTime.now());
            documentTagMapper.insert(dt);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTagsFromDocument(Long documentId, List<Long> tagIds) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            LambdaQueryWrapper<DocumentTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DocumentTag::getDocumentId, documentId);
            wrapper.eq(DocumentTag::getTagId, tagId);
            documentTagMapper.delete(wrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDocumentTags(Long documentId, List<Long> tagIds) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        List<Long> current = documentTagMapper.findTagIdsByDocumentId(documentId);
        List<Long> toAdd = new java.util.ArrayList<>();
        List<Long> toRemove = new java.util.ArrayList<>();
        if (tagIds != null) {
            for (Long id : tagIds) {
                if (current == null || !current.contains(id)) toAdd.add(id);
            }
        }
        if (current != null) {
            for (Long id : current) {
                if (tagIds == null || !tagIds.contains(id)) toRemove.add(id);
            }
        }
        if (!toAdd.isEmpty()) addTagsToDocument(documentId, toAdd);
        if (!toRemove.isEmpty()) removeTagsFromDocument(documentId, toRemove);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tag getOrCreateTag(String name, Long projectId, Long creatorId) {
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("标签名称不能为空");
        }
        Tag existing = tagMapper.findByName(name, projectId);
        if (existing != null) return existing;
        Tag tag = new Tag();
        tag.setName(name.trim());
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        tag.setDeleted(false);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Tag> ensureTagsExist(List<String> tagNames) {
        List<Tag> result = new ArrayList<>();
        if (tagNames == null || tagNames.isEmpty()) {
            return result;
        }
        for (String name : tagNames) {
            if (StringUtils.isBlank(name)) continue;
            Tag existing = tagMapper.findByName(name, null);
            if (existing != null) {
                result.add(existing);
            } else {
                Tag tag = new Tag();
                tag.setName(name.trim());
                tag.setDeleted(false);
                tag.setCreateTime(LocalDateTime.now());
                tag.setUpdateTime(LocalDateTime.now());
                tagMapper.insert(tag);
                result.add(tag);
            }
        }
        return result;
    }
}


