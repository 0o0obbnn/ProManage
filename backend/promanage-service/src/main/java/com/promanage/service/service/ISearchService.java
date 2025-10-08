package com.promanage.service.service;

import com.promanage.common.domain.PageResult;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface ISearchService {
    
    /**
     * 搜索结果内部类
     */
    class SearchResult {
        private String type;        // 类型: document, project, task
        private Long id;            // ID
        private String title;       // 标题
        private String content;     // 内容
        private String highlightedContent; // 高亮内容
        private String author;      // 作者
        private String createdTime; // 创建时间
        private String updatedTime; // 更新时间
        private Long projectId;     // 项目ID（如果有）
        private String projectName; // 项目名称（如果有）
        
        public SearchResult() {}
        
        public SearchResult(String type, Long id, String title, String content, 
                          String highlightedContent, String author, String createdTime, 
                          String updatedTime, Long projectId, String projectName) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.content = content;
            this.highlightedContent = highlightedContent;
            this.author = author;
            this.createdTime = createdTime;
            this.updatedTime = updatedTime;
            this.projectId = projectId;
            this.projectName = projectName;
        }
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getHighlightedContent() {
            return highlightedContent;
        }
        
        public void setHighlightedContent(String highlightedContent) {
            this.highlightedContent = highlightedContent;
        }
        
        public String getAuthor() {
            return author;
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public String getCreatedTime() {
            return createdTime;
        }
        
        public void setCreatedTime(String createdTime) {
            this.createdTime = createdTime;
        }
        
        public String getUpdatedTime() {
            return updatedTime;
        }
        
        public void setUpdatedTime(String updatedTime) {
            this.updatedTime = updatedTime;
        }
        
        public Long getProjectId() {
            return projectId;
        }
        
        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }
        
        public String getProjectName() {
            return projectName;
        }
        
        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }
    }
    
    /**
     * 全局搜索
     * @param keyword 关键词
     * @param type 类型过滤（可选）
     * @param projectId 项目过滤（可选）
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<SearchResult> globalSearch(String keyword, String type, Long projectId, 
                                         Integer page, Integer pageSize);
    
    /**
     * 搜索文档
     * @param keyword 关键词
     * @param projectId 项目过滤（可选）
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<SearchResult> searchDocuments(String keyword, Long projectId, 
                                           Integer page, Integer pageSize);
    
    /**
     * 搜索项目
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<SearchResult> searchProjects(String keyword, Integer page, Integer pageSize);
    
    /**
     * 搜索任务
     * @param keyword 关键词
     * @param projectId 项目过滤（可选）
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<SearchResult> searchTasks(String keyword, Long projectId, 
                                        Integer page, Integer pageSize);
    
    /**
     * 获取搜索建议
     * @param keyword 关键词
     * @return 建议列表
     */
    List<String> getSearchSuggestions(String keyword);
    
    /**
     * 高亮显示内容
     * @param content 原始内容
     * @param keyword 关键词
     * @return 高亮后的内容
     */
    String highlightContent(String content, String keyword);
}