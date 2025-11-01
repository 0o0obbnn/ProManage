package com.promanage.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promanage.common.result.PageResult;
import com.promanage.service.dto.SearchResultDTO;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.service.ISearchService;

@ExtendWith(MockitoExtension.class)
@DisplayName("搜索服务实现测试")
class SearchServiceImplTest {

  @Mock private DocumentMapper documentMapper;

  @Mock private ProjectMapper projectMapper;

  @Mock private TaskMapper taskMapper;

  @InjectMocks private SearchServiceImpl searchService;

  private SearchResultDTO testDocument;
  private SearchResultDTO testProject;
  private SearchResultDTO testTask;

  @BeforeEach
  void setUp() {
    // 创建测试文档
    testDocument = new SearchResultDTO();
    testDocument.setId(1L);
    testDocument.setTitle("测试文档");
    testDocument.setContent("这是一个测试文档的内容");
    testDocument.setProjectId(1L);
    testDocument.setCreatorName("张三");
    testDocument.setProjectName("测试项目");
    testDocument.setCreateTime(LocalDateTime.now());
    testDocument.setUpdateTime(LocalDateTime.now());

    // 创建测试项目
    testProject = new SearchResultDTO();
    testProject.setId(1L);
    testProject.setTitle("测试项目");
    testProject.setContent("这是一个测试项目的描述");
    testProject.setCreatorId(1L);
    testProject.setCreatorName("李四");
    testProject.setCreateTime(LocalDateTime.now());
    testProject.setUpdateTime(LocalDateTime.now());

    // 创建测试任务
    testTask = new SearchResultDTO();
    testTask.setId(1L);
    testTask.setTitle("测试任务");
    testTask.setContent("这是一个测试任务的描述");
    testTask.setProjectId(1L);
    testTask.setAssigneeId(2L);
    testTask.setAssigneeName("王五");
    testTask.setProjectName("测试项目");
    testTask.setCreateTime(LocalDateTime.now());
    testTask.setUpdateTime(LocalDateTime.now());
  }

  @Test
  @DisplayName("全局搜索 - 成功")
  void testGlobalSearchSuccess() {
    // 准备测试数据
    List<SearchResultDTO> documents = Arrays.asList(testDocument);
    List<SearchResultDTO> projects = Arrays.asList(testProject);
    List<SearchResultDTO> tasks = Arrays.asList(testTask);

    // 模拟Mapper返回
    when(documentMapper.searchDocuments(eq("测试"), isNull(), eq(0), eq(10))).thenReturn(documents);
    when(documentMapper.countSearchDocuments(eq("测试"), isNull())).thenReturn(1L);

    when(projectMapper.searchProjects(eq("测试"), eq(0), eq(10))).thenReturn(projects);
    when(projectMapper.countSearchProjects(eq("测试"))).thenReturn(1L);

    when(taskMapper.searchTasks(eq("测试"), isNull(), eq(0), eq(10))).thenReturn(tasks);
    when(taskMapper.countSearchTasks(eq("测试"), isNull())).thenReturn(1L);

    // 执行搜索
    PageResult<ISearchService.SearchResult> result =
        searchService.globalSearch("测试", null, null, 1, 10);

    // 验证结果
    assertNotNull(result);
    assertEquals(3, result.getList().size());
    assertEquals(3, result.getTotal().intValue());

    // 验证结果类型
    assertEquals("document", result.getList().get(0).getType());
    assertEquals("project", result.getList().get(1).getType());
    assertEquals("task", result.getList().get(2).getType());
  }

  @Test
  @DisplayName("全局搜索 - 按类型过滤")
  void testGlobalSearchWithTypeFilter() {
    // 准备测试数据
    List<SearchResultDTO> documents = Arrays.asList(testDocument);

    // 模拟Mapper返回
    when(documentMapper.searchDocuments(eq("测试"), isNull(), eq(0), eq(10))).thenReturn(documents);
    when(documentMapper.countSearchDocuments(eq("测试"), isNull())).thenReturn(1L);

    // 执行搜索
    PageResult<ISearchService.SearchResult> result =
        searchService.globalSearch("测试", "document", null, 1, 10);

    // 验证结果
    assertNotNull(result);
    assertEquals(1, result.getList().size());
    assertEquals(1, result.getTotal().intValue());
    assertEquals("document", result.getList().get(0).getType());
  }

  @Test
  @DisplayName("全局搜索 - 空关键词")
  void testGlobalSearchWithEmptyKeyword() {
    // 执行搜索
    PageResult<ISearchService.SearchResult> result =
        searchService.globalSearch("", null, null, 1, 10);

    // 验证结果
    assertNotNull(result);
    assertEquals(0, result.getList().size());
    assertEquals(0, result.getTotal().intValue());
  }

  @Test
  @DisplayName("搜索文档 - 成功")
  void testSearchDocumentsSuccess() {
    // 准备测试数据
    List<SearchResultDTO> documents = Arrays.asList(testDocument);

    // 模拟Mapper返回
    when(documentMapper.searchDocuments(eq("测试"), isNull(), eq(0), eq(10))).thenReturn(documents);
    when(documentMapper.countSearchDocuments(eq("测试"), isNull())).thenReturn(1L);

    // 执行搜索
    PageResult<ISearchService.SearchResult> result =
        searchService.searchDocuments("测试", null, 1, 10);

    // 验证结果
    assertNotNull(result);
    assertEquals(1, result.getList().size());
    assertEquals(1, result.getTotal().intValue());
    assertEquals("document", result.getList().get(0).getType());
    assertEquals("测试文档", result.getList().get(0).getTitle());
  }

  @Test
  @DisplayName("搜索项目 - 成功")
  void testSearchProjectsSuccess() {
    // 准备测试数据
    List<SearchResultDTO> projects = Arrays.asList(testProject);

    // 模拟Mapper返回
    when(projectMapper.searchProjects(eq("测试"), eq(0), eq(10))).thenReturn(projects);
    when(projectMapper.countSearchProjects(eq("测试"))).thenReturn(1L);

    // 执行搜索
    PageResult<ISearchService.SearchResult> result = searchService.searchProjects("测试", 1, 10);

    // 验证结果
    assertNotNull(result);
    assertEquals(1, result.getList().size());
    assertEquals(1, result.getTotal().intValue());
    assertEquals("project", result.getList().get(0).getType());
    assertEquals("测试项目", result.getList().get(0).getTitle());
  }

  @Test
  @DisplayName("搜索任务 - 成功")
  void testSearchTasksSuccess() {
    // 准备测试数据
    List<SearchResultDTO> tasks = Arrays.asList(testTask);

    // 模拟Mapper返回
    when(taskMapper.searchTasks(eq("测试"), isNull(), eq(0), eq(10))).thenReturn(tasks);
    when(taskMapper.countSearchTasks(eq("测试"), isNull())).thenReturn(1L);

    // 执行搜索
    PageResult<ISearchService.SearchResult> result = searchService.searchTasks("测试", null, 1, 10);

    // 验证结果
    assertNotNull(result);
    assertEquals(1, result.getList().size());
    assertEquals(1, result.getTotal().intValue());
    assertEquals("task", result.getList().get(0).getType());
    assertEquals("测试任务", result.getList().get(0).getTitle());
  }

  @Test
  @DisplayName("获取搜索建议 - 成功")
  void testGetSearchSuggestionsSuccess() {
    // 准备测试数据
    List<String> docTitles = Arrays.asList("测试文档1", "测试文档2");
    List<String> projNames = Arrays.asList("测试项目1", "测试项目2");
    List<String> taskTitles = Arrays.asList("测试任务1", "测试任务2");

    // 模拟Mapper返回
    when(documentMapper.getDistinctTitlesByKeyword(eq("测试"))).thenReturn(docTitles);
    when(projectMapper.getDistinctNamesByKeyword(eq("测试"))).thenReturn(projNames);
    when(taskMapper.getDistinctTitlesByKeyword(eq("测试"))).thenReturn(taskTitles);

    // 执行搜索
    List<String> suggestions = searchService.getSearchSuggestions("测试");

    // 验证结果
    assertNotNull(suggestions);
    assertEquals(6, suggestions.size());
    assertTrue(suggestions.contains("测试文档1"));
    assertTrue(suggestions.contains("测试项目1"));
    assertTrue(suggestions.contains("测试任务1"));
  }

  @Test
  @DisplayName("获取搜索建议 - 空关键词")
  void testGetSearchSuggestionsWithEmptyKeyword() {
    // 执行搜索
    List<String> suggestions = searchService.getSearchSuggestions("");

    // 验证结果
    assertNotNull(suggestions);
    assertEquals(0, suggestions.size());
  }

  @Test
  @DisplayName("获取搜索建议 - 关键词太短")
  void testGetSearchSuggestionsWithShortKeyword() {
    // 执行搜索
    List<String> suggestions = searchService.getSearchSuggestions("测");

    // 验证结果
    assertNotNull(suggestions);
    assertEquals(0, suggestions.size());
  }

  @Test
  @DisplayName("高亮内容 - 成功")
  void testHighlightContentSuccess() {
    // 执行高亮
    String highlighted = searchService.highlightContent("这是一个测试文档的内容", "测试");

    // 验证结果
    assertNotNull(highlighted);
    assertTrue(highlighted.contains("<mark>测试</mark>"));
  }

  @Test
  @DisplayName("高亮内容 - 空内容")
  void testHighlightContentWithEmptyContent() {
    // 执行高亮
    String highlighted = searchService.highlightContent("", "测试");

    // 验证结果
    assertEquals("", highlighted);
  }

  @Test
  @DisplayName("高亮内容 - 空关键词")
  void testHighlightContentWithEmptyKeyword() {
    // 执行高亮
    String highlighted = searchService.highlightContent("这是一个测试文档的内容", "");

    // 验证结果
    assertEquals("这是一个测试文档的内容", highlighted);
  }

  @Test
  @DisplayName("高亮内容 - 多个关键词")
  void testHighlightContentWithMultipleKeywords() {
    // 执行高亮
    String highlighted = searchService.highlightContent("这是一个测试文档的内容", "测试 文档");

    // 验证结果
    assertNotNull(highlighted);
    assertTrue(highlighted.contains("<mark>测试</mark>"));
    assertTrue(highlighted.contains("<mark>文档</mark>"));
  }
}
