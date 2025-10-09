package com.promanage.api.controller;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Document;
import com.promanage.service.mapper.CommentMapper;
import com.promanage.service.service.IDocumentFolderService;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDocumentService documentService;
    @MockitoBean
    private IDocumentFolderService documentFolderService;
    @MockitoBean
    private IUserService userService;
    @MockitoBean
    private CommentMapper commentMapper;
    
    // Mock security components
    @MockitoBean
    private com.promanage.infrastructure.security.JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private com.promanage.infrastructure.security.TokenBlacklistService tokenBlacklistService;
    @MockitoBean
    private com.promanage.infrastructure.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // Removed ObjectMapper as it's not needed in this test

    @Test
    void searchDocuments_shouldReturnDocuments() throws Exception {
        // Given
        PageResult<Document> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);
        when(documentService.searchDocuments(anyInt(), anyInt(), anyLong(), anyInt(), anyString(), 
                anyLong(), anyLong(), anyString(), any(), any()))
                .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/search")
                .param("page", "1")
                .param("size", "20")
                .param("projectId", "1")
                .param("status", "2")
                .param("keyword", "test")
                .param("folderId", "3")
                .param("creatorId", "4")
                .param("type", "PRD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    void searchDocuments_withTimeRange_shouldReturnDocuments() throws Exception {
        // Given
        PageResult<Document> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);
        when(documentService.searchDocuments(anyInt(), anyInt(), anyLong(), anyInt(), anyString(), 
                anyLong(), anyLong(), anyString(), any(), any()))
                .thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/search")
                .param("page", "1")
                .param("size", "20")
                .param("startTime", "2023-01-01T00:00:00")
                .param("endTime", "2023-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }
}