package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promanage.dto.ProjectStatsDTO;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.Task;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.TaskMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目统计策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectStatsStrategy {

    private final ProjectMemberMapper projectMemberMapper;
    private final TaskMapper taskMapper;
    private final DocumentMapper documentMapper;
    private final ChangeRequestMapper changeRequestMapper;

    /**
     * 获取项目统计信息
     */
    public ProjectStatsDTO getProjectStats(Long projectId) {
        ProjectStatsDTO stats = new ProjectStatsDTO();
        stats.setProjectId(projectId);
        
        // 成员统计
        long memberCount = getMemberCount(projectId);
        stats.setMemberCount((int) memberCount);
        
        // 任务统计
        Map<String, Integer> taskStats = getTaskStats(projectId);
        stats.setTaskStats(taskStats);
        
        // 文档统计
        Map<String, Integer> documentStats = getDocumentStats(projectId);
        stats.setDocumentStats(documentStats);
        
        // 变更请求统计
        Map<String, Integer> changeRequestStats = getChangeRequestStats(projectId);
        stats.setChangeRequestStats(changeRequestStats);
        
        // 活动统计
        Map<String, Integer> activityStats = getActivityStats(projectId);
        stats.setActivityStats(activityStats);
        
        return stats;
    }

    /**
     * 获取成员数量
     */
    private long getMemberCount(Long projectId) {
        return projectMemberMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.promanage.service.entity.ProjectMember>()
                .eq(com.promanage.service.entity.ProjectMember::getProjectId, projectId)
        );
    }

    /**
     * 获取任务统计
     */
    private Map<String, Integer> getTaskStats(Long projectId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 总任务数
        long totalTasks = taskMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
        );
        stats.put("total", (int) totalTasks);
        
        // 待办任务数
        long pendingTasks = taskMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, "PENDING")
        );
        stats.put("pending", (int) pendingTasks);
        
        // 进行中任务数
        long inProgressTasks = taskMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, "IN_PROGRESS")
        );
        stats.put("inProgress", (int) inProgressTasks);
        
        // 已完成任务数
        long completedTasks = taskMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                .eq(com.promanage.service.entity.Task::getProjectId, projectId)
                .eq(com.promanage.service.entity.Task::getStatus, "COMPLETED")
        );
        stats.put("completed", (int) completedTasks);
        
        return stats;
    }

    /**
     * 获取文档统计
     */
    private Map<String, Integer> getDocumentStats(Long projectId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 总文档数
        long totalDocuments = documentMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                .eq(com.promanage.service.entity.Document::getProjectId, projectId)
        );
        stats.put("total", (int) totalDocuments);
        
        // 草稿文档数
        long draftDocuments = documentMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                .eq(com.promanage.service.entity.Document::getProjectId, projectId)
                .eq(com.promanage.service.entity.Document::getStatus, "DRAFT")
        );
        stats.put("draft", (int) draftDocuments);
        
        // 已发布文档数
        long publishedDocuments = documentMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Document>()
                .eq(com.promanage.service.entity.Document::getProjectId, projectId)
                .eq(com.promanage.service.entity.Document::getStatus, "PUBLISHED")
        );
        stats.put("published", (int) publishedDocuments);
        
        return stats;
    }

    /**
     * 获取变更请求统计
     */
    private Map<String, Integer> getChangeRequestStats(Long projectId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 总变更请求数
        long totalChangeRequests = changeRequestMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChangeRequest>()
                .eq(com.promanage.service.entity.ChangeRequest::getProjectId, projectId)
        );
        stats.put("total", (int) totalChangeRequests);
        
        // 待审批变更请求数
        long pendingChangeRequests = changeRequestMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChangeRequest>()
                .eq(com.promanage.service.entity.ChangeRequest::getProjectId, projectId)
                .eq(com.promanage.service.entity.ChangeRequest::getStatus, "PENDING_APPROVAL")
        );
        stats.put("pending", (int) pendingChangeRequests);
        
        // 已批准变更请求数
        long approvedChangeRequests = changeRequestMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChangeRequest>()
                .eq(com.promanage.service.entity.ChangeRequest::getProjectId, projectId)
                .eq(com.promanage.service.entity.ChangeRequest::getStatus, "APPROVED")
        );
        stats.put("approved", (int) approvedChangeRequests);
        
        return stats;
    }

    /**
     * 获取活动统计
     */
    private Map<String, Integer> getActivityStats(Long projectId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 今日活动数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayActivities = getActivityCount(projectId, today, LocalDateTime.now());
        stats.put("today", (int) todayActivities);
        
        // 本周活动数
        LocalDateTime weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        long weekActivities = getActivityCount(projectId, weekStart, LocalDateTime.now());
        stats.put("thisWeek", (int) weekActivities);
        
        // 本月活动数
        LocalDateTime monthStart = today.withDayOfMonth(1);
        long monthActivities = getActivityCount(projectId, monthStart, LocalDateTime.now());
        stats.put("thisMonth", (int) monthActivities);
        
        return stats;
    }

    /**
     * 获取活动数量（简化实现）
     */
    private long getActivityCount(Long projectId, LocalDateTime startTime, LocalDateTime endTime) {
        // 这里应该查询活动表，简化实现返回0
        return 0;
    }
}
