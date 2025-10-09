package com.promanage.service.service.impl;

import com.promanage.service.mapper.DocumentCommentMapper;
import com.promanage.service.service.IDocumentCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentCommentServiceImpl implements IDocumentCommentService {

    private final DocumentCommentMapper documentCommentMapper;

    @Override
    public int countByDocumentId(Long documentId) {
        try {
            return documentCommentMapper.countByDocumentId(documentId);
        } catch (Exception e) {
            log.error("统计评论数失败, documentId={}", documentId, e);
            return 0;
        }
    }
}


