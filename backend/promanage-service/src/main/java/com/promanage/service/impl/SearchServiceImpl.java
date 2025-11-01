package com.promanage.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.promanage.common.result.PageResult;
import com.promanage.service.dto.SearchResultDTO;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.service.ISearchService;

import lombok.extern.slf4j.Slf4j;

/** 搜索服务实现类 */
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

  private final DocumentMapper documentMapper;

  private final ProjectMapper projectMapper;

  private final TaskMapper taskMapper;

  public SearchServiceImpl(
      DocumentMapper documentMapper, ProjectMapper projectMapper, TaskMapper taskMapper) {
    this.documentMapper = documentMapper;
    this.projectMapper = projectMapper;
    this.taskMapper = taskMapper;
  }

  @Override
  public PageResult<SearchResult> globalSearch(
      String keyword, String type, Long projectId, Integer page, Integer pageSize) {
    if (!StringUtils.hasText(keyword)) {
      return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
    }

    List<SearchResult> allResults = new ArrayList<>();
    long totalCount = 0;

    // 根据类型过滤搜索
    if (!StringUtils.hasText(type) || "document".equals(type)) {
      PageResult<SearchResult> docResults = searchDocuments(keyword, projectId, page, pageSize);
      allResults.addAll(docResults.getList());
      totalCount += docResults.getTotal();
    }

    if (!StringUtils.hasText(type) || "project".equals(type)) {
      PageResult<SearchResult> projResults = searchProjects(keyword, page, pageSize);
      allResults.addAll(projResults.getList());
      totalCount += projResults.getTotal();
    }

    if (!StringUtils.hasText(type) || "task".equals(type)) {
      PageResult<SearchResult> taskResults = searchTasks(keyword, projectId, page, pageSize);
      allResults.addAll(taskResults.getList());
      totalCount += taskResults.getTotal();
    }

    // 分页处理
    int startIndex = (page - 1) * pageSize;
    int endIndex = Math.min(startIndex + pageSize, allResults.size());

    List<SearchResult> pageResults = new ArrayList<>();
    if (startIndex < allResults.size()) {
      pageResults = allResults.subList(startIndex, endIndex);
    }

    return PageResult.of(pageResults, totalCount, page, pageSize);
  }

  @Override
  public PageResult<SearchResult> searchDocuments(
      String keyword, Long projectId, Integer page, Integer pageSize) {
    if (!StringUtils.hasText(keyword)) {
      return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
    }

    // 查询文档
    List<SearchResultDTO> documents =
        documentMapper.searchDocuments(keyword, projectId, (page - 1) * pageSize, pageSize);

    // 查询总数
    Long total = documentMapper.countSearchDocuments(keyword, projectId);

    // 转换为搜索结果
    List<SearchResult> results =
        documents.stream()
            .map(
                doc -> {
                  String highlightedContent = highlightContent(doc.getContent(), keyword);
                  return new SearchResult(
                      "document",
                      doc.getId(),
                      doc.getTitle(),
                      doc.getContent(),
                      highlightedContent,
                      doc.getCreatorName(),
                      doc.getCreateTime() != null ? doc.getCreateTime().toString() : null,
                      doc.getUpdateTime() != null ? doc.getUpdateTime().toString() : null,
                      doc.getProjectId(),
                      doc.getProjectName());
                })
            .collect(Collectors.toList());

    return PageResult.of(results, total, page, pageSize);
  }

  @Override
  public PageResult<SearchResult> searchProjects(String keyword, Integer page, Integer pageSize) {
    if (!StringUtils.hasText(keyword)) {
      return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
    }

    // 查询项目
    List<SearchResultDTO> projects =
        projectMapper.searchProjects(keyword, (page - 1) * pageSize, pageSize);

    // 查询总数
    Long total = projectMapper.countSearchProjects(keyword);

    // 转换为搜索结果
    List<SearchResult> results =
        projects.stream()
            .map(
                proj -> {
                  String highlightedContent = highlightContent(proj.getContent(), keyword);
                  return new SearchResult(
                      "project",
                      proj.getId(),
                      proj.getTitle(),
                      proj.getContent(),
                      highlightedContent,
                      proj.getCreatorName(),
                      proj.getCreateTime() != null ? proj.getCreateTime().toString() : null,
                      proj.getUpdateTime() != null ? proj.getUpdateTime().toString() : null,
                      null,
                      null);
                })
            .collect(Collectors.toList());

    return PageResult.of(results, total, page, pageSize);
  }

  @Override
  public PageResult<SearchResult> searchTasks(
      String keyword, Long projectId, Integer page, Integer pageSize) {
    if (!StringUtils.hasText(keyword)) {
      return PageResult.of(new ArrayList<>(), 0L, page, pageSize);
    }

    // 查询任务
    List<SearchResultDTO> tasks =
        taskMapper.searchTasks(keyword, projectId, (page - 1) * pageSize, pageSize);

    // 查询总数
    Long total = taskMapper.countSearchTasks(keyword, projectId);

    // 转换为搜索结果
    List<SearchResult> results =
        tasks.stream()
            .map(
                task -> {
                  String highlightedContent = highlightContent(task.getContent(), keyword);
                  return new SearchResult(
                      "task",
                      task.getId(),
                      task.getTitle(),
                      task.getContent(),
                      highlightedContent,
                      task.getAssigneeName(),
                      task.getCreateTime() != null ? task.getCreateTime().toString() : null,
                      task.getUpdateTime() != null ? task.getUpdateTime().toString() : null,
                      task.getProjectId(),
                      task.getProjectName());
                })
            .collect(Collectors.toList());

    return PageResult.of(results, total, page, pageSize);
  }

  @Override
  public List<String> getSearchSuggestions(String keyword) {
    if (!StringUtils.hasText(keyword) || keyword.length() < 2) {
      return new ArrayList<>();
    }

    List<String> suggestions = new ArrayList<>();

    // 获取文档标题建议
    List<String> docTitles = documentMapper.getDistinctTitlesByKeyword(keyword);
    suggestions.addAll(docTitles);

    // 获取项目名称建议
    List<String> projNames = projectMapper.getDistinctNamesByKeyword(keyword);
    suggestions.addAll(projNames);

    // 获取任务标题建议
    List<String> taskTitles = taskMapper.getDistinctTitlesByKeyword(keyword);
    suggestions.addAll(taskTitles);

    // 去重并限制数量
    return suggestions.stream().distinct().limit(10).collect(Collectors.toList());
  }

  @Override
  public String highlightContent(String content, String keyword) {
    if (!StringUtils.hasText(content) || !StringUtils.hasText(keyword)) {
      return content;
    }

    // 简单的关键词高亮实现
    String[] words = keyword.split("\\s+");
    String highlighted = content;

    for (String word : words) {
      if (StringUtils.hasText(word)) {
        // 使用正则表达式替换，忽略大小写
        highlighted =
            highlighted.replaceAll(
                "(?i)" + java.util.regex.Pattern.quote(word), "<mark>" + word + "</mark>");
      }
    }

    return highlighted;
  }
}
