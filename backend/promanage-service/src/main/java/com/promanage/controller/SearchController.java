package com.promanage.controller;

import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.Result;
import com.promanage.service.service.ISearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索管理", description = "搜索相关API")
public class SearchController {
    
    @Autowired
    private ISearchService searchService;
    
    @GetMapping
    @Operation(summary = "全局搜索", description = "在文档、项目、任务中进行全局搜索")
    public Result<PageResult<ISearchService.SearchResult>> globalSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "类型过滤: document/project/task") @RequestParam(required = false) String type,
            @Parameter(description = "项目ID过滤") @RequestParam(required = false) Long projectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return Result.error(400, "搜索关键词不能为空");
            }
            
            PageResult<ISearchService.SearchResult> result = searchService.globalSearch(
                    keyword, type, projectId, page, pageSize);
            
            return Result.success("搜索成功", result);
        } catch (Exception e) {
            log.error("全局搜索失败: {}", e.getMessage(), e);
            return Result.error(500, "搜索失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/suggest")
    @Operation(summary = "搜索建议", description = "根据关键词获取搜索建议")
    public Result<List<String>> getSearchSuggestions(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return Result.error(400, "搜索关键词不能为空");
            }
            
            List<String> suggestions = searchService.getSearchSuggestions(keyword);
            return Result.success("获取搜索建议成功", suggestions);
        } catch (Exception e) {
            log.error("获取搜索建议失败: {}", e.getMessage(), e);
            return Result.error(500, "获取搜索建议失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/documents")
    @Operation(summary = "搜索文档", description = "在文档中进行搜索")
    public Result<PageResult<ISearchService.SearchResult>> searchDocuments(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "项目ID过滤") @RequestParam(required = false) Long projectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return Result.error(400, "搜索关键词不能为空");
            }
            
            PageResult<ISearchService.SearchResult> result = searchService.searchDocuments(
                    keyword, projectId, page, pageSize);
            
            return Result.success("搜索文档成功", result);
        } catch (Exception e) {
            log.error("搜索文档失败: {}", e.getMessage(), e);
            return Result.error(500, "搜索文档失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/projects")
    @Operation(summary = "搜索项目", description = "在项目中进行搜索")
    public Result<PageResult<ISearchService.SearchResult>> searchProjects(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return Result.error(400, "搜索关键词不能为空");
            }
            
            PageResult<ISearchService.SearchResult> result = searchService.searchProjects(
                    keyword, page, pageSize);
            
            return Result.success("搜索项目成功", result);
        } catch (Exception e) {
            log.error("搜索项目失败: {}", e.getMessage(), e);
            return Result.error(500, "搜索项目失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/tasks")
    @Operation(summary = "搜索任务", description = "在任务中进行搜索")
    public Result<PageResult<ISearchService.SearchResult>> searchTasks(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "项目ID过滤") @RequestParam(required = false) Long projectId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return Result.error(400, "搜索关键词不能为空");
            }
            
            PageResult<ISearchService.SearchResult> result = searchService.searchTasks(
                    keyword, projectId, page, pageSize);
            
            return Result.success("搜索任务成功", result);
        } catch (Exception e) {
            log.error("搜索任务失败: {}", e.getMessage(), e);
            return Result.error(500, "搜索任务失败: " + e.getMessage());
        }
    }
}