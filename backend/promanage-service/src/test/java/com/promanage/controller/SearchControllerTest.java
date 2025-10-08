package com.promanage.controller;

import com.promanage.common.domain.PageResult;
import com.promanage.config.TestConfig;
import com.promanage.service.service.ISearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SearchController.class)
@ContextConfiguration(classes = {SearchController.class, TestConfig.class})
@DisplayName("搜索控制器测试")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ISearchService searchService;

    

    private ISearchService.SearchResult documentResult;
    private ISearchService.SearchResult projectResult;
    private ISearchService.SearchResult taskResult;
    private PageResult<ISearchService.SearchResult> searchPageResult;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        documentResult = new ISearchService.SearchResult(
                "document", 1L, "测试文档", "这是一个测试文档的内容", 
                "这是一个<mark>测试</mark>文档的内容", "张三", 
                "2023-01-01", "2023-01-01", 1L, "测试项目"
        );

        projectResult = new ISearchService.SearchResult(
                "project", 1L, "测试项目", "这是一个测试项目的描述", 
                "这是一个<mark>测试</mark>项目的描述", "李四", 
                "2023-01-01", "2023-01-01", null, null
        );

        taskResult = new ISearchService.SearchResult(
                "task", 1L, "测试任务", "这是一个测试任务的描述", 
                "这是一个<mark>测试</mark>任务的描述", "王五", 
                "2023-01-01", "2023-01-01", 1L, "测试项目"
        );

        List<ISearchService.SearchResult> searchResults = Arrays.asList(
                documentResult, projectResult, taskResult
        );
        searchPageResult = PageResult.of(searchResults, 3L, 1, 10);
    }

    @Test
    @DisplayName("全局搜索 - 成功")
    void testGlobalSearchSuccess() throws Exception {
        // 模拟服务层返回
        when(searchService.globalSearch(anyString(), anyString(), isNull(), anyInt(), anyInt()))
                .thenReturn(searchPageResult);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/search")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("搜索成功"));
    }

    @Test
    @DisplayName("全局搜索 - 按类型过滤")
    void testGlobalSearchWithTypeFilter() throws Exception {
        // 只返回文档类型的结果
        List<ISearchService.SearchResult> documentResults = Arrays.asList(documentResult);
        PageResult<ISearchService.SearchResult> documentPageResult = 
                PageResult.of(documentResults, 1L, 1, 10);

        when(searchService.globalSearch(eq("测试"), eq("document"), isNull(), eq(1), eq(10)))
                .thenReturn(documentPageResult);

        mockMvc.perform(get("/api/search")
                        .param("keyword", "测试")
                        .param("type", "document")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].type").value("document"));
    }

    @Test
    @DisplayName("全局搜索 - 按项目过滤")
    void testGlobalSearchWithProjectFilter() throws Exception {
        when(searchService.globalSearch(eq("测试"), isNull(), eq(1L), eq(1), eq(10)))
                .thenReturn(searchPageResult);

        mockMvc.perform(get("/api/search")
                        .param("keyword", "测试")
                        .param("projectId", "1")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    @DisplayName("全局搜索 - 缺少关键字参数")
    void testGlobalSearchMissingKeyword() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("搜索建议 - 成功")
    void testSearchSuggestionsSuccess() throws Exception {
        List<String> suggestions = Arrays.asList("测试文档", "测试项目", "测试任务");
        when(searchService.getSearchSuggestions(eq("测试"))).thenReturn(suggestions);

        mockMvc.perform(get("/api/search/suggest")
                        .param("keyword", "测试")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取搜索建议成功"))
                .andExpect(jsonPath("$.data[0]").value("测试文档"))
                .andExpect(jsonPath("$.data[1]").value("测试项目"))
                .andExpect(jsonPath("$.data[2]").value("测试任务"));
    }

    @Test
    @DisplayName("搜索建议 - 缺少关键字参数")
    void testSearchSuggestionsMissingKeyword() throws Exception {
        mockMvc.perform(get("/api/search/suggest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("文档搜索 - 成功")
    void testSearchDocumentsSuccess() throws Exception {
        List<ISearchService.SearchResult> documentResults = Arrays.asList(documentResult);
        PageResult<ISearchService.SearchResult> documentPageResult = 
                PageResult.of(documentResults, 1L, 1, 10);

        when(searchService.searchDocuments(eq("测试"), isNull(), eq(1), eq(10)))
                .thenReturn(documentPageResult);

        mockMvc.perform(get("/api/search/documents")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("搜索文档成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].type").value("document"));
    }

    @Test
    @DisplayName("项目搜索 - 成功")
    void testSearchProjectsSuccess() throws Exception {
        List<ISearchService.SearchResult> projectResults = Arrays.asList(projectResult);
        PageResult<ISearchService.SearchResult> projectPageResult = 
                PageResult.of(projectResults, 1L, 1, 10);

        when(searchService.searchProjects(eq("测试"), eq(1), eq(10)))
                .thenReturn(projectPageResult);

        mockMvc.perform(get("/api/search/projects")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("搜索项目成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].type").value("project"));
    }

    @Test
    @DisplayName("任务搜索 - 成功")
    void testSearchTasksSuccess() throws Exception {
        List<ISearchService.SearchResult> taskResults = Arrays.asList(taskResult);
        PageResult<ISearchService.SearchResult> taskPageResult = 
                PageResult.of(taskResults, 1L, 1, 10);

        when(searchService.searchTasks(eq("测试"), isNull(), eq(1), eq(10)))
                .thenReturn(taskPageResult);

        mockMvc.perform(get("/api/search/tasks")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("搜索任务成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].type").value("task"));
    }

    @Test
    @DisplayName("搜索服务异常处理")
    void testSearchServiceException() throws Exception {
        when(searchService.globalSearch(anyString(), anyString(), isNull(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("搜索服务异常"));

        mockMvc.perform(get("/api/search")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("搜索成功"));
    }
}