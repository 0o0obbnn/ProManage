package com.promanage.service.strategy;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.result.PageResult;
import com.promanage.service.entity.Task;
import com.promanage.service.mapper.TaskMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务查询策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class TaskQueryStrategy {

    private final TaskMapper taskMapper;

    /**
     * 根据ID获取任务
     */
    public Task getTaskById(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    /**
     * 分页查询任务列表
     */
    public PageResult<Task> listTasks(Long projectId, int page, int pageSize, 
                                    String keyword, Integer status, Long assigneeId, 
                                    Long reporterId, String priority, Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq(Task::getProjectId, projectId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Task::getTitle, keyword)
                .or()
                .like(Task::getDescription, keyword)
            );
        }
        
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        
        if (assigneeId != null) {
            wrapper.eq(Task::getAssigneeId, assigneeId);
        }
        
        if (reporterId != null) {
            wrapper.eq(Task::getReporterId, reporterId);
        }
        
        if (priority != null && !priority.trim().isEmpty()) {
            wrapper.eq(Task::getPriority, priority);
        }
        
        wrapper.orderByDesc(Task::getCreateTime);
        
        IPage<Task> pageResult = taskMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Task>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 分页查询指派人任务
     */
    public PageResult<Task> listTasksByAssignee(Long assigneeId, int page, int pageSize, 
                                              Integer status, String priority, Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getAssigneeId, assigneeId);
        
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        
        if (priority != null && !priority.trim().isEmpty()) {
            wrapper.eq(Task::getPriority, priority);
        }
        
        wrapper.orderByDesc(Task::getCreateTime);
        
        IPage<Task> pageResult = taskMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Task>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 分页查询报告人任务
     */
    public PageResult<Task> listTasksByReporter(Long reporterId, int page, int pageSize, 
                                              Integer status, String priority, Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getReporterId, reporterId);
        
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        
        if (priority != null && !priority.trim().isEmpty()) {
            wrapper.eq(Task::getPriority, priority);
        }
        
        wrapper.orderByDesc(Task::getCreateTime);
        
        IPage<Task> pageResult = taskMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<Task>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 获取子任务列表
     */
    public List<Task> listSubtasks(Long parentTaskId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getParentTaskId, parentTaskId);
        wrapper.orderByAsc(Task::getCreateTime);
        return taskMapper.selectList(wrapper);
    }

    /**
     * 获取任务依赖列表
     */
    public List<Task> listTaskDependencies(Long taskId) {
        // 这里应该通过任务依赖表查询
        // 简化实现，返回空列表
        return java.util.Collections.emptyList();
    }

    /**
     * 统计项目任务数量
     */
    public int countTasksByProject(Long projectId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        return Math.toIntExact(taskMapper.selectCount(wrapper));
    }

    /**
     * 统计指派人任务数量
     */
    public int countTasksByAssignee(Long userId, Integer status) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getAssigneeId, userId);
        
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        return Math.toIntExact(taskMapper.selectCount(wrapper));
    }

    /**
     * 检查任务权限
     */
    public boolean hasTaskPermission(Long taskId, Long userId) {
        Task task = getTaskById(taskId);
        if (task == null) {
            return false;
        }
        
        // 检查是否是任务指派人、报告人或项目成员
        return task.getAssigneeId().equals(userId) || 
               task.getReporterId().equals(userId) ||
               hasProjectAccess(task.getProjectId(), userId);
    }

    /**
     * 检查任务查看权限
     */
    public boolean hasTaskViewPermission(Long taskId, Long userId) {
        return hasTaskPermission(taskId, userId);
    }

    /**
     * 检查项目访问权限（简化实现）
     */
    private boolean hasProjectAccess(Long projectId, Long userId) {
        // 这里应该通过ProjectMember表检查用户是否有项目访问权限
        // 简化实现，返回true
        return true;
    }
}
