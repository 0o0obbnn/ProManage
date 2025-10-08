package com.promanage.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Document;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.DocumentVersionMapper;
import com.promanage.infrastructure.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceImplTest {

    @Mock
    private DocumentMapper documentMapper;
    
    @Mock
    private DocumentVersionMapper documentVersionMapper;
    
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchDocuments_withAllParameters_shouldReturnFilteredResults() {
        // Given
        Long projectId = 1L;
        Integer status = 2;
        String keyword = "test";
        Long folderId = 3L;
        Long creatorId = 4L;
        String type = "PRD";
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 31, 23, 59);

        Page<Document> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(documentMapper.selectPage(any(), any())).thenReturn(page);

        // When
        PageResult<Document> result = documentService.searchDocuments(
                1, 20, projectId, status, keyword, folderId, creatorId, type, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(documentMapper, times(1)).selectPage(any(), any());
    }

    @Test
    void searchDocuments_withMinimalParameters_shouldReturnResults() {
        // Given
        Page<Document> page = new Page<>(1, 20);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(documentMapper.selectPage(any(), any())).thenReturn(page);

        // When
        PageResult<Document> result = documentService.searchDocuments(
                1, 20, null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        verify(documentMapper, times(1)).selectPage(any(), any());
    }
}