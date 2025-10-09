package com.promanage.service.service;

import com.promanage.service.entity.Document;

import java.util.List;

public interface IDocumentRelationService {
    /**
     * 根据共享标签获取相关文档（简单策略）
     */
    List<Document> findRelatedByTags(Long documentId, int limit);
}


