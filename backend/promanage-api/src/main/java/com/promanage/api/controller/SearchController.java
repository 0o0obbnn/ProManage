package com.promanage.api.controller;

import com.promanage.api.dto.response.SearchResponse;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.Task;
import com.promanage.service.service.ISearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索控制器
 * <p>
 * 提供全局搜索、文档搜索、项目搜索、任务搜索等功能
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-06
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "搜索管理", description = "搜索相关接口")
@RequiredArgsConstructor
public class SearchController {

    private final ISearchService searchService;

    /**
     * 全局搜索
     *
     * @param q         搜索关键词
     * @param type      搜索类型 (document/project/task/all)
     * @param projectId 项目ID过滤
     * @param page      页码
     * @param size      每页大小
     * @return 搜索结果
     */
    @GetMapping
    @Operation(summary = "全局搜索", description = "在文档、项目、任务中进行全局搜索")
    public Result<PageResult<SearchResponse>> globalSearch(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String q,
            @Parameter(description = "搜索类型") @RequestParam(defaultValue = "all") String type,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        log.info("全局搜索请求, q={}, type={}, projectId={}, page={}, size={}", 
                q, type, projectId, page, size);

        PageResult<ISearchService.SearchResult> searchResults = 
                searchService.globalSearch(q, type, projectId, page, size);

        // 转换为响应DTO
        List<SearchResponse> responseList = searchResults.getList().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageResult<SearchResponse> response = PageResult.of(
                responseList,
                searchResults.getTotal(),
                searchResults.getPage(),
                searchResults.getPageSize()
        );

        log.info("全局搜索完成, q={}, 结果数量={}", q, responseList.size());
        return Result.success(response);
    }

    /**
     * 获取搜索建议
     *
     * @param q     搜索关键词
     * @param limit 返回数量限制
     * @return 搜索建议列表
     */
    @GetMapping("/suggest")
    @Operation(summary = "搜索建议", description = "根据关键词提供搜索建议")
    public Result<List<String>> getSuggestions(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String q,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取搜索建议请求, q={}, limit={}", q, limit);

        List<String> suggestions = searchService.getSearchSuggestions(q);

        log.info("搜索建议获取完成, q={}, 建议数量={}", q, suggestions.size());
        return Result.success(suggestions);
    }

    /**
     * 搜索文档
     *
     * @param q         搜索关键词
     * @param projectId 项目ID过滤
     * @param page      页码
     * @param size      每页大小
     * @return 文档搜索结果
     */
    @GetMapping("/documents")
    @Operation(summary = "搜索文档", description = "在文档中进行搜索")
    public Result<PageResult<Document>> searchDocuments(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String q,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        log.info("搜索文档请求, q={}, projectId={}, page={}, size={}", 
                q, projectId, page, size);

        PageResult<ISearchService.SearchResult> searchResults = searchService.searchDocuments(q, projectId, page, size);
        PageResult<Document> documents = convertSearchResultToDocumentPage(searchResults);

        log.info("文档搜索完成, q={}, 结果数量={}", q, documents.getList().size());
        return Result.success(documents);
    }

    /**
     * 搜索项目
     *
     * @param q    搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 项目搜索结果
     */
    @GetMapping("/projects")
    @Operation(summary = "搜索项目", description = "在项目中进行搜索")
    public Result<PageResult<Project>> searchProjects(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String q,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        log.info("搜索项目请求, q={}, page={}, size={}", q, page, size);

        PageResult<ISearchService.SearchResult> searchResults = searchService.searchProjects(q, page, size);
        PageResult<Project> projects = convertSearchResultToProjectPage(searchResults);

        log.info("项目搜索完成, q={}, 结果数量={}", q, projects.getList().size());
        return Result.success(projects);
    }

    /**
     * 搜索任务
     *
     * @param q         搜索关键词
     * @param projectId 项目ID过滤
     * @param page      页码
     * @param size      每页大小
     * @return 任务搜索结果
     */
    @GetMapping("/tasks")
    @Operation(summary = "搜索任务", description = "在任务中进行搜索")
    public Result<PageResult<Task>> searchTasks(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String q,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {

        log.info("搜索任务请求, q={}, projectId={}, page={}, size={}", 
                q, projectId, page, size);

        PageResult<ISearchService.SearchResult> searchResults = searchService.searchTasks(q, projectId, page, size);
        PageResult<Task> tasks = convertSearchResultToTaskPage(searchResults);

        log.info("任务搜索完成, q={}, 结果数量={}", q, tasks.getList().size());
        return Result.success(tasks);
    }

    /**
     * 将搜索结果转换为响应DTO
     *
     * @param searchResult 搜索结果
     * @return 响应DTO
     */
    private SearchResponse convertToResponse(ISearchService.SearchResult searchResult) {
        SearchResponse response = new SearchResponse();
        response.setType(searchResult.getType());
        response.setId(searchResult.getId());
        response.setTitle(searchResult.getTitle());
        response.setContent(searchResult.getContent());
        response.setHighlightedContent(searchResult.getHighlightedContent());
        response.setLink(generateLink(searchResult));
        response.setProjectId(searchResult.getProjectId());
        response.setProjectName(searchResult.getProjectName());
        response.setCreatedAt(parseCreatedAt(searchResult));
        return response;
    }
    
    /**
     * 根据搜索结果类型生成链接
     *
     * @param searchResult 搜索结果
     * @return 链接地址
     */
    private String generateLink(ISearchService.SearchResult searchResult) {
        String type = searchResult.getType();
        Long id = searchResult.getId();
        
        switch (type) {
            case "document":
                return "/documents/" + id;
            case "project":
                return "/projects/" + id;
            case "task":
                return "/tasks/" + id;
            default:
                return "#";
        }
    }
    
    /**
     * 解析创建时间
     *
     * @param searchResult 搜索结果
     * @return 创建时间
     */
    private LocalDateTime parseCreatedAt(ISearchService.SearchResult searchResult) {
        // 将字符串时间转换为LocalDateTime
        String createdTime = searchResult.getCreatedTime();
        if (createdTime != null && !createdTime.isEmpty()) {
            try {
                return LocalDateTime.parse(createdTime);
            } catch (Exception e) {
                log.warn("无法解析创建时间: {}", createdTime, e);
                return null;
            }
        }
        return null;
    }
    
    /**
     * 将搜索结果转换为文档分页结果
     *
     * @param searchResults 搜索结果
     * @return 文档分页结果
     */
    private PageResult<Document> convertSearchResultToDocumentPage(PageResult<ISearchService.SearchResult> searchResults) {
        // 这里需要实现从SearchResult到Document的转换逻辑
        // 由于SearchResult只包含基本信息，可能需要调用其他服务获取完整Document对象
        // 暂时返回空结果，实际实现需要根据业务逻辑补充
        return PageResult.of(new java.util.ArrayList<>(), 0L, searchResults.getPage(), searchResults.getPageSize());
    }
    
    /**
     * 将搜索结果转换为项目分页结果
     *
     * @param searchResults 搜索结果
     * @return 项目分页结果
     */
    private PageResult<Project> convertSearchResultToProjectPage(PageResult<ISearchService.SearchResult> searchResults) {
        // 这里需要实现从SearchResult到Project的转换逻辑
        // 由于SearchResult只包含基本信息，可能需要调用其他服务获取完整Project对象
        // 暂时返回空结果，实际实现需要根据业务逻辑补充
        return PageResult.of(new java.util.ArrayList<>(), 0L, searchResults.getPage(), searchResults.getPageSize());
    }
    
    /**
     * 将搜索结果转换为任务分页结果
     *
     * @param searchResults 搜索结果
     * @return 任务分页结果
     */
    private PageResult<Task> convertSearchResultToTaskPage(PageResult<ISearchService.SearchResult> searchResults) {
        // 这里需要实现从SearchResult到Task的转换逻辑
        // 由于SearchResult只包含基本信息，可能需要调用其他服务获取完整Task对象
        // 暂时返回空结果，实际实现需要根据业务逻辑补充
        return PageResult.of(new java.util.ArrayList<>(), 0L, searchResults.getPage(), searchResults.getPageSize());
    }
}