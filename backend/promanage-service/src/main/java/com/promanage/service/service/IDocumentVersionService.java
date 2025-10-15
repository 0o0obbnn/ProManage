package com.promanage.service.service;

import com.promanage.service.entity.DocumentVersion;
import java.util.List;

/**
 * 文档版本服务接口
 */
public interface IDocumentVersionService {
    
    /**
     * 获取文档版本列表
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 版本列表
     */
    List<DocumentVersion> listVersions(Long documentId, Long userId);
}