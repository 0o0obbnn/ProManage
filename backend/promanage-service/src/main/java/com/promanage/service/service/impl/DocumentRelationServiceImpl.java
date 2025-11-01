package com.promanage.service.service.impl;

import org.springframework.dao.DataAccessException;
import java.util.*;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.promanage.service.entity.Document;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentTagMapper;
import com.promanage.service.service.IDocumentRelationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentRelationServiceImpl implements IDocumentRelationService {

  private final DocumentTagMapper documentTagMapper;
  private final DocumentMapper documentMapper;

  @Override
  public List<Document> findRelatedByTags(Long documentId, int limit) {
    try {
      List<Long> tagIds = documentTagMapper.findTagIdsByDocumentId(documentId);
      if (tagIds == null || tagIds.isEmpty()) return List.of();

      // gather doc ids sharing any tag
      Set<Long> relatedDocIds = new HashSet<>();
      for (Long tagId : tagIds) {
        List<Long> docIds = documentTagMapper.findDocumentIdsByTagId(tagId);
        if (docIds != null) relatedDocIds.addAll(docIds);
      }
      relatedDocIds.remove(documentId);
      if (relatedDocIds.isEmpty()) return List.of();

      // fetch limited documents ordered by update time desc, filter deleted
      LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
      wrapper
          .in(Document::getId, relatedDocIds)
          .eq(Document::getDeleted, false)
          .orderByDesc(Document::getUpdateTime)
          .last("LIMIT " + Math.max(1, limit));
      return documentMapper.selectList(wrapper);
    } catch (DataAccessException e) {
      log.warn("查找关联文档失败, documentId={}", documentId, e);
      return List.of();
    }
  }
}
