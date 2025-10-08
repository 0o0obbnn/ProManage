package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.dto.TaskTimeTrackingDTO;
import com.promanage.service.ITaskTimeTrackingService;
import com.promanage.service.service.ITaskService;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.TaskTimeTracking;
import com.promanage.service.mapper.TaskTimeTrackingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务时间追踪服务实现类
 * </p>
 *
 * @author ProManage Team
 * @since 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskTimeTrackingServiceImpl extends ServiceImpl<TaskTimeTrackingMapper, TaskTimeTracking> 
        implements ITaskTimeTrackingService {

    private final ITaskService taskService;

    @Override
    public PageResult<TaskTimeTrackingDTO> listTimeTracking(Long taskId, Long userId, Long projectId, 
                                                          Integer status, LocalDate startDate, LocalDate endDate,
                                                          Integer page, Integer size) {
        log.info("查询任务时间追踪记录: taskId={}, userId={}, projectId={}, status={}, startDate={}, endDate={}, page={}, size={}", 
                taskId, userId, projectId, status, startDate, endDate, page, size);

        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        
        if (taskId != null) {
            wrapper.eq(TaskTimeTracking::getTaskId, taskId);
        }
        if (userId != null) {
            wrapper.eq(TaskTimeTracking::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(TaskTimeTracking::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(TaskTimeTracking::getWorkDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(TaskTimeTracking::getWorkDate, endDate);
        }
        
        // 如果指定了项目ID，需要关联查询任务表
        if (projectId != null) {
            // 由于没有直接的方法获取项目下的所有任务，暂时返回空列表
            List<Long> taskIds = java.util.List.of();
            
            if (taskIds.isEmpty()) {
                return PageResult.of(List.of(), 0L, page, size);
            }
            wrapper.in(TaskTimeTracking::getTaskId, taskIds);
        }
        
        wrapper.orderByDesc(TaskTimeTracking::getCreateTime);
        
        IPage<TaskTimeTracking> pageResult = page(new Page<>(page, size), wrapper);
        
        List<TaskTimeTrackingDTO> dtoList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return PageResult.of(dtoList, pageResult.getTotal(), page, size);
    }

    @Override
    public List<TaskTimeTrackingDTO> getTimeTrackingByTaskId(Long taskId) {
        log.info("根据任务ID获取时间追踪记录: taskId={}", taskId);
        
        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTimeTracking::getTaskId, taskId);
        wrapper.orderByDesc(TaskTimeTracking::getCreateTime);
        
        List<TaskTimeTracking> list = list(wrapper);
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskTimeTrackingDTO> getTimeTrackingByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("根据用户ID获取时间追踪记录: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTimeTracking::getUserId, userId);
        
        if (startDate != null) {
            wrapper.ge(TaskTimeTracking::getWorkDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(TaskTimeTracking::getWorkDate, endDate);
        }
        
        wrapper.orderByDesc(TaskTimeTracking::getCreateTime);
        
        List<TaskTimeTracking> list = list(wrapper);
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public TaskTimeTrackingDTO getTimeTrackingById(Long id) {
        log.info("根据ID获取时间追踪记录: id={}", id);
        
        TaskTimeTracking timeTracking = getById(id);
        if (timeTracking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间追踪记录不存在");
        }
        
        return convertToDTO(timeTracking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTimeTracking(TaskTimeTrackingDTO timeTrackingDTO) {
        log.info("创建时间追踪记录: taskId={}, userId={}", timeTrackingDTO.getTaskId(), timeTrackingDTO.getUserId());
        
        // 验证任务是否存在
        Task task = taskService.getTaskById(timeTrackingDTO.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        
        // 验证工作日期不能为空
        if (timeTrackingDTO.getWorkDate() == null) {
            timeTrackingDTO.setWorkDate(LocalDate.now());
        }
        
        TaskTimeTracking timeTracking = new TaskTimeTracking();
        BeanUtils.copyProperties(timeTrackingDTO, timeTracking);
        timeTracking.setStatus(0); // 默认未开始
        timeTracking.setCreateTime(LocalDateTime.now());
        timeTracking.setUpdateTime(LocalDateTime.now());
        
        save(timeTracking);
        return timeTracking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTimeTracking(Long id, TaskTimeTrackingDTO timeTrackingDTO) {
        log.info("更新时间追踪记录: id={}", id);
        
        TaskTimeTracking existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间追踪记录不存在");
        }
        
        BeanUtils.copyProperties(timeTrackingDTO, existing);
        existing.setId(id);
        existing.setUpdateTime(LocalDateTime.now());
        
        return updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTimeTracking(Long id) {
        log.info("删除时间追踪记录: id={}", id);
        
        TaskTimeTracking existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间追踪记录不存在");
        }
        
        // 检查是否为进行中的状态，如果是则不允许删除
        if (existing.getStatus() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "进行中的时间追踪记录不能删除");
        }
        
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startTimeTracking(Long taskId, Long userId, String workDescription) {
        log.info("开始任务计时: taskId={}, userId={}", taskId, userId);
        
        // 验证任务是否存在
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        
        // 检查是否有进行中的计时
        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTimeTracking::getTaskId, taskId);
        wrapper.eq(TaskTimeTracking::getUserId, userId);
        wrapper.eq(TaskTimeTracking::getStatus, 1); // 进行中
        TaskTimeTracking existing = getOne(wrapper);
        
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该任务已有进行中的计时");
        }
        
        TaskTimeTracking timeTracking = new TaskTimeTracking();
        timeTracking.setTaskId(taskId);
        timeTracking.setUserId(userId);
        timeTracking.setWorkDescription(workDescription);
        timeTracking.setWorkDate(LocalDate.now());
        timeTracking.setStartTime(LocalDateTime.now());
        timeTracking.setStatus(1); // 进行中
        timeTracking.setCreateTime(LocalDateTime.now());
        timeTracking.setUpdateTime(LocalDateTime.now());
        
        save(timeTracking);
        return timeTracking.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endTimeTracking(Long id) {
        log.info("结束任务计时: id={}", id);
        
        TaskTimeTracking timeTracking = getById(id);
        if (timeTracking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间追踪记录不存在");
        }
        
        if (timeTracking.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能结束进行中的计时");
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        timeTracking.setEndTime(endTime);
        timeTracking.setStatus(2); // 已完成
        timeTracking.setUpdateTime(endTime);
        
        // 计算实际工时
        if (timeTracking.getStartTime() != null) {
            long minutes = java.time.Duration.between(timeTracking.getStartTime(), endTime).toMinutes();
            timeTracking.setActualHours(minutes / 60.0);
        }
        
        return updateById(timeTracking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseTimeTracking(Long id) {
        log.info("暂停任务计时: id={}", id);
        
        TaskTimeTracking timeTracking = getById(id);
        if (timeTracking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间追踪记录不存在");
        }
        
        if (timeTracking.getStatus() != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能暂停进行中的计时");
        }
        
        timeTracking.setStatus(3); // 已暂停
        timeTracking.setUpdateTime(LocalDateTime.now());
        
        return updateById(timeTracking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resumeTimeTracking(Long id) {
        log.info("恢复任务计时: id={}", id);
        
        TaskTimeTracking timeTracking = getById(id);
        if (timeTracking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间追踪记录不存在");
        }
        
        if (timeTracking.getStatus() != 3) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "只能恢复已暂停的计时");
        }
        
        timeTracking.setStatus(1); // 进行中
        timeTracking.setUpdateTime(LocalDateTime.now());
        
        return updateById(timeTracking);
    }

    @Override
    public Double getTotalHoursByTask(Long taskId) {
        log.info("获取任务总工时: taskId={}", taskId);
        
        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTimeTracking::getTaskId, taskId);
        wrapper.eq(TaskTimeTracking::getStatus, 2); // 只统计已完成的记录
        
        List<TaskTimeTracking> list = list(wrapper);
        return list.stream()
                .mapToDouble(t -> t.getActualHours() != null ? t.getActualHours() : 0.0)
                .sum();
    }

    @Override
    public Double getTotalHoursByUser(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("获取用户在指定时间范围内的总工时: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTimeTracking::getUserId, userId);
        wrapper.eq(TaskTimeTracking::getStatus, 2); // 只统计已完成的记录
        
        if (startDate != null) {
            wrapper.ge(TaskTimeTracking::getWorkDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(TaskTimeTracking::getWorkDate, endDate);
        }
        
        List<TaskTimeTracking> list = list(wrapper);
        return list.stream()
                .mapToDouble(t -> t.getActualHours() != null ? t.getActualHours() : 0.0)
                .sum();
    }

    @Override
    public Double getTotalHoursByProject(Long projectId) {
        log.info("获取项目总工时: projectId={}", projectId);
        
        // 获取项目下的所有任务
        // 由于没有直接的方法获取项目下的所有任务，暂时返回空列表
        List<Long> taskIds = java.util.List.of();
        
        if (taskIds.isEmpty()) {
            return 0.0;
        }
        
        // 获取这些任务的时间追踪记录
        LambdaQueryWrapper<TaskTimeTracking> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TaskTimeTracking::getTaskId, taskIds);
        wrapper.eq(TaskTimeTracking::getStatus, 2); // 只统计已完成的记录
        
        List<TaskTimeTracking> list = list(wrapper);
        return list.stream()
                .mapToDouble(t -> t.getActualHours() != null ? t.getActualHours() : 0.0)
                .sum();
    }

    /**
     * 转换为DTO
     */
    private TaskTimeTrackingDTO convertToDTO(TaskTimeTracking timeTracking) {
        TaskTimeTrackingDTO dto = new TaskTimeTrackingDTO();
        BeanUtils.copyProperties(timeTracking, dto);
        
        // 查询任务信息
        Task task = taskService.getTaskById(timeTracking.getTaskId());
        if (task != null) {
            dto.setTaskName(task.getTitle());
            dto.setProjectId(task.getProjectId());
            
            // 查询项目信息
            // 这里可以添加项目查询逻辑，但由于当前没有项目服务注入，暂时留空
            // dto.setProjectName(project.getName());
        }
        
        return dto;
    }
}
