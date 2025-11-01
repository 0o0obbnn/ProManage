package com.promanage.service.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.service.entity.DocumentTag;
import com.promanage.service.mapper.DocumentTagMapper;
import com.promanage.service.service.IDocumentTagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentTagServiceImpl implements IDocumentTagService {

  private final DocumentTagMapper documentTagMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void setDocumentTags(Long documentId, List<Long> tagIds) {
    if (documentId == null) return;
    // clear existing
    documentTagMapper.deleteByDocumentId(documentId);
    if (tagIds == null || tagIds.isEmpty()) return;
    for (Long tagId : tagIds) {
      DocumentTag dt = new DocumentTag();
      dt.setDocumentId(documentId);
      dt.setTagId(tagId);
      dt.setCreatedAt(LocalDateTime.now());
      documentTagMapper.insert(dt);
    }
  }

  @Override
  public List<Long> getDocumentTagIds(Long documentId) {
    if (documentId == null) return List.of();
    return documentTagMapper.findTagIdsByDocumentId(documentId);
  }
}
