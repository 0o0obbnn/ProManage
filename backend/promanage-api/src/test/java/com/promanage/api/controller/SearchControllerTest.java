package com.promanage.api.controller;

import com.promanage.common.domain.PageResult;
import com.promanage.service.service.ISearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISearchService searchService;

    @Test
    void globalSearch_withValidParameters_shouldReturnResults() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> searchResult = PageResult.of(
                Arrays.asList(
                        createTestSearchResult("document", 1L, "Test Document", "Test content"),
                        createTestSearchResult("project", 1L, "Test Project", "Test description")
                ), 
                2L, 1, 20
        );
        when(searchService.globalSearch(anyString(), anyString(), any(), anyInt(), anyInt()))
                .thenReturn(searchResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search")
                .param("q", "test")
                .param("type", "all")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void globalSearch_withProjectFilter_shouldReturnFilteredResults() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> searchResult = PageResult.of(
                Arrays.asList(
                        createTestSearchResult("document", 1L, "Test Document", "Test content")
                ), 
                1L, 1, 20
        );
        when(searchService.globalSearch(anyString(), anyString(), eq(1L), anyInt(), anyInt()))
                .thenReturn(searchResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search")
                .param("q", "test")
                .param("type", "document")
                .param("projectId", "1")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void getSearchSuggestions_withValidKeyword_shouldReturnSuggestions() throws Exception {
        // Given
        List<String> suggestions = Arrays.asList("Test Document", "Test Project", "Test Task");
        when(searchService.getSearchSuggestions(anyString())).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/search/suggest")
                .param("q", "test")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void searchDocuments_withValidKeyword_shouldReturnDocuments() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> documentResult = PageResult.of(
                Arrays.asList(createTestSearchResult("document", 1L, "Test Document", "Test content")),
                1L, 1, 20
        );
        when(searchService.searchDocuments(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(documentResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search/documents")
                .param("q", "test")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void searchDocuments_withProjectFilter_shouldReturnFilteredDocuments() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> documentResult = PageResult.of(
                Arrays.asList(createTestSearchResult("document", 1L, "Test Document", "Test content")),
                1L, 1, 20
        );
        when(searchService.searchDocuments(anyString(), eq(1L), anyInt(), anyInt()))
                .thenReturn(documentResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search/documents")
                .param("q", "test")
                .param("projectId", "1")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void searchProjects_withValidKeyword_shouldReturnProjects() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> projectResult = PageResult.of(
                Arrays.asList(createTestSearchResult("project", 1L, "Test Project", "Test description")),
                1L, 1, 20
        );
        when(searchService.searchProjects(anyString(), anyInt(), anyInt()))
                .thenReturn(projectResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search/projects")
                .param("q", "test")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void searchTasks_withValidKeyword_shouldReturnTasks() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> taskResult = PageResult.of(
                Arrays.asList(createTestSearchResult("task", 1L, "Test Task", "Test task description")),
                1L, 1, 20
        );
        when(searchService.searchTasks(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(taskResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search/tasks")
                .param("q", "test")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void searchTasks_withProjectFilter_shouldReturnFilteredTasks() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> taskResult = PageResult.of(
                Arrays.asList(createTestSearchResult("task", 1L, "Test Task", "Test task description")),
                1L, 1, 20
        );
        when(searchService.searchTasks(anyString(), eq(1L), anyInt(), anyInt()))
                .thenReturn(taskResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search/tasks")
                .param("q", "test")
                .param("projectId", "1")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void globalSearch_withEmptyResults_shouldReturnEmptyList() throws Exception {
        // Given
        PageResult<ISearchService.SearchResult> searchResult = PageResult.of(
                Collections.emptyList(), 0L, 1, 20
        );
        when(searchService.globalSearch(anyString(), anyString(), any(), anyInt(), anyInt()))
                .thenReturn(searchResult);

        // When & Then
        mockMvc.perform(get("/api/v1/search")
                .param("q", "nonexistent")
                .param("type", "all")
                .param("page", "1")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // Helper methods to create test entities
    private ISearchService.SearchResult createTestSearchResult(String type, Long id, String title, String content) {
        return new ISearchService.SearchResult(
                type, id, title, content, content, 
                "testuser", "2025-10-07T10:00:00", "2025-10-07T10:00:00", 1L, "Test Project"
        );
    }

    
}