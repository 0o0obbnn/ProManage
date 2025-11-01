package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.entity.TaskCheckItem;
import com.promanage.service.mapper.TaskCheckItemMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务检查项策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class TaskCheckItemStrategy {

    private final TaskCheckItemMapper taskCheckItemMapper;

    /**
     * 获取任务检查项列表
     */
    public List<TaskCheckItem> listTaskCheckItems(Long taskId) {
        LambdaQueryWrapper<TaskCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCheckItem::getTaskId, taskId);
        wrapper.orderByAsc(TaskCheckItem::getSortOrder);
        return taskCheckItemMapper.selectList(wrapper);
    }

    /**
     * 添加任务检查项
     */
    public Long addTaskCheckItem(TaskCheckItem checkItem) {
        validateCheckItem(checkItem);
        
        checkItem.setCreateTime(LocalDateTime.now());
        checkItem.setUpdateTime(LocalDateTime.now());
        
        // 设置排序顺序
        if (checkItem.getSortOrder() == null) {
            checkItem.setSortOrder(getNextSortOrder(checkItem.getTaskId()));
        }
        
        taskCheckItemMapper.insert(checkItem);
        return checkItem.getId();
    }

    /**
     * 更新任务检查项
     */
    public void updateTaskCheckItem(TaskCheckItem checkItem) {
        validateCheckItem(checkItem);
        
        TaskCheckItem existingItem = getTaskCheckItemById(checkItem.getId());
        if (existingItem == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "检查项不存在");
        }
        
        checkItem.setUpdateTime(LocalDateTime.now());
        taskCheckItemMapper.updateById(checkItem);
    }

    /**
     * 删除任务检查项
     */
    public void deleteTaskCheckItem(Long checkItemId, Long userId) {
        TaskCheckItem checkItem = getTaskCheckItemById(checkItemId);
        if (checkItem == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "检查项不存在");
        }
        
        // 检查权限：只有任务指派人或报告人可以删除
        if (!hasTaskPermission(checkItem.getTaskId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此检查项");
        }
        
        taskCheckItemMapper.deleteById(checkItemId);
    }

    /**
     * 根据ID获取任务检查项
     */
    public TaskCheckItem getTaskCheckItemById(Long checkItemId) {
        return taskCheckItemMapper.selectById(checkItemId);
    }

    /**
     * 获取任务检查项数量
     */
    public int getCheckItemCount(Long taskId) {
        LambdaQueryWrapper<TaskCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCheckItem::getTaskId, taskId);
        return Math.toIntExact(taskCheckItemMapper.selectCount(wrapper));
    }

    /**
     * 获取已完成检查项数量
     */
    public int getCompletedCheckItemCount(Long taskId) {
        LambdaQueryWrapper<TaskCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCheckItem::getTaskId, taskId);
        wrapper.eq(TaskCheckItem::getIsCompleted, true);
        return Math.toIntExact(taskCheckItemMapper.selectCount(wrapper));
    }

    /**
     * 获取下一个排序顺序
     */
    private Integer getNextSortOrder(Long taskId) {
        LambdaQueryWrapper<TaskCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCheckItem::getTaskId, taskId);
        wrapper.orderByDesc(TaskCheckItem::getSortOrder);
        wrapper.last("LIMIT 1");
        
        TaskCheckItem lastItem = taskCheckItemMapper.selectOne(wrapper);
        return lastItem != null ? lastItem.getSortOrder() + 1 : 1;
    }

    /**
     * 检查任务权限（简化实现）
     */
    private boolean hasTaskPermission(Long taskId, Long userId) {
        // 这里应该通过TaskService检查用户是否有任务权限
        // 简化实现，返回true
        return true;
    }

    /**
     * 验证检查项
     */
    private void validateCheckItem(TaskCheckItem checkItem) {
        if (checkItem == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "检查项信息不能为空");
        }
        if (checkItem.getTaskId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "任务ID不能为空");
        }
        if (checkItem.getContent() == null || checkItem.getContent().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "检查项标题不能为空");
        }
    }
}
