package com.promanage.service.service.impl;

import com.promanage.service.entity.DocumentVersion;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.service.service.IDocumentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 文档版本服务实现
 */
@Service
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements IDocumentVersionService {
    
    private final DocumentVersionMapper documentVersionMapper;
    
    @Override
    public List<DocumentVersion> listVersions(Long documentId, Long userId) {
        return documentVersionMapper.findByDocumentId(documentId);
    }
}