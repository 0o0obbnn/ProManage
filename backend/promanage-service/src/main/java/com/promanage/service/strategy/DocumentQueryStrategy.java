package com.promanage.service.strategy;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.result.PageResult;
import com.promanage.service.dto.request.DocumentSearchRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.mapper.DocumentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档查询策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class DocumentQueryStrategy {

    private final DocumentMapper documentMapper;

    /**
     * 根据ID获取文档
     */
    public Document getById(Long id) {
        return documentMapper.selectById(id);
    }

    /**
     * 分页查询项目文档
     */
    public PageResult<Document> listByProject(Long projectId, int page, int pageSize, 
                                            String keyword, String status, String type, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getProjectId, projectId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Document::getTitle, keyword)
                .or()
                .like(Document::getContent, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Document::getStatus, status);
        }
        
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(Document::getType, type);
        }
        
        wrapper.orderByDesc(Document::getCreateTime);
        
        IPage<Document> pageResult = documentMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Document>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 获取项目文档列表
     */
    public List<Document> listByProject(Long projectId, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getProjectId, projectId);
        wrapper.orderByDesc(Document::getCreateTime);
        return documentMapper.selectList(wrapper);
    }

    /**
     * 获取创建者的文档列表
     */
    public List<Document> listByCreator(Long creatorId, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getCreatorId, creatorId);
        wrapper.orderByDesc(Document::getCreateTime);
        return documentMapper.selectList(wrapper);
    }

    /**
     * 分页查询所有文档
     */
    public PageResult<Document> listAllDocuments(int page, int pageSize, String keyword, 
                                               String status, String type, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Document::getTitle, keyword)
                .or()
                .like(Document::getContent, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Document::getStatus, status);
        }
        
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(Document::getType, type);
        }
        
        wrapper.orderByDesc(Document::getCreateTime);
        
        IPage<Document> pageResult = documentMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Document>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 搜索文档
     */
    public PageResult<Document> searchDocuments(DocumentSearchRequest request, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Document::getTitle, request.getKeyword())
                .or()
                .like(Document::getContent, request.getKeyword())
            );
        }
        
        if (request.getProjectId() != null) {
            wrapper.eq(Document::getProjectId, request.getProjectId());
        }
        
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            wrapper.eq(Document::getStatus, request.getStatus());
        }
        
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            wrapper.eq(Document::getType, request.getType());
        }
        
        if (request.getCreatorId() != null) {
            wrapper.eq(Document::getCreatorId, request.getCreatorId());
        }
        
        wrapper.orderByDesc(Document::getCreateTime);
        
        IPage<Document> pageResult = documentMapper.selectPage(
            new Page<>(request.getPage(), request.getPageSize()), wrapper);
        
        return PageResult.<Document>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(request.getPage())
            .pageSize(request.getPageSize())
            .build();
    }

    /**
     * 统计项目文档数量
     */
    public int countByProject(Long projectId, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getProjectId, projectId);
        return Math.toIntExact(documentMapper.selectCount(wrapper)); // Long → int
    }

    /**
     * 统计创建者文档数量
     */
    public int countByCreator(Long creatorId, Long userId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getCreatorId, creatorId);
        return Math.toIntExact(documentMapper.selectCount(wrapper)); // Long → int
    }

    /**
     * 获取项目名称映射
     */
    public Map<Long, String> getProjectNames(List<Long> projectIds, Long userId) {
        // 这里应该通过ProjectService获取项目名称
        // 简化实现，返回空Map
        return java.util.Collections.emptyMap();
    }

    /**
     * 检查文档是否存在
     */
    public boolean existsById(Long id) {
        return documentMapper.selectById(id) != null;
    }
}
