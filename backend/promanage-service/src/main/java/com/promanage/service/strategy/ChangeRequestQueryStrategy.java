package com.promanage.service.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.enums.ChangeRequestStatus;
import com.promanage.common.result.PageResult;
import com.promanage.service.dto.request.ChangeRequestQueryRequest;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.mapper.ChangeRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 变更请求查询策略
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
@Component
@RequiredArgsConstructor
public class ChangeRequestQueryStrategy {

    private final ChangeRequestMapper changeRequestMapper;

    /**
     * 分页查询变更请求
     */
    public PageResult<ChangeRequest> queryChangeRequests(ChangeRequestQueryRequest queryRequest) {
        Long projectId = queryRequest.getProjectId();
        Integer page = queryRequest.getPage();
        Integer pageSize = queryRequest.getPageSize();
        String keyword = queryRequest.getKeyword();
        Integer status = queryRequest.getStatus();
        String priority = queryRequest.getPriority();
        Long requesterId = queryRequest.getRequesterId();
        Long assigneeId = queryRequest.getAssigneeId();
        Long approverId = queryRequest.getApproverId();
        String startDate = queryRequest.getStartDate();
        String endDate = queryRequest.getEndDate();
        // 参数处理
        page = (page == null || page < 1) ? 1 : page;
        pageSize = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        
        // 构建查询条件
        LambdaQueryWrapper<ChangeRequest> wrapper = buildQueryWrapper(
            projectId, keyword, status, priority, requesterId, assigneeId, approverId, startDate, endDate);
        
        // 分页查询
        IPage<ChangeRequest> pageResult = changeRequestMapper.selectPage(
            new Page<>(page, pageSize), wrapper);
        
        return PageResult.<ChangeRequest>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ChangeRequest> buildQueryWrapper(Long projectId, String keyword, 
                                                               Integer status, String priority, 
                                                               Long requesterId, Long assigneeId, 
                                                               Long approverId, String startDate, 
                                                               String endDate) {
        LambdaQueryWrapper<ChangeRequest> wrapper = new LambdaQueryWrapper<>();
        
        // 项目ID条件
        if (projectId != null) {
            wrapper.eq(ChangeRequest::getProjectId, projectId);
        }
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(ChangeRequest::getTitle, keyword)
                .or()
                .like(ChangeRequest::getDescription, keyword)
            );
        }
        
        // 状态条件
        if (status != null) {
            wrapper.eq(ChangeRequest::getStatus, status);
        }
        
        // 优先级条件
        if (StringUtils.hasText(priority)) {
            wrapper.eq(ChangeRequest::getPriority, priority);
        }
        
        // 请求人条件
        if (requesterId != null) {
            wrapper.eq(ChangeRequest::getRequesterId, requesterId);
        }
        
        // 指派人条件
        if (assigneeId != null) {
            wrapper.eq(ChangeRequest::getAssigneeId, assigneeId);
        }
        
        // 审批人条件
        if (approverId != null) {
            wrapper.eq(ChangeRequest::getApproverId, approverId);
        }
        
        // 日期范围条件
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(ChangeRequest::getCreateTime, startDate);
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(ChangeRequest::getCreateTime, endDate);
        }
        
        // 排序
        wrapper.orderByDesc(ChangeRequest::getCreateTime);
        
        return wrapper;
    }

    /**
     * 根据ID查询变更请求
     */
    public ChangeRequest findById(Long id) {
        return changeRequestMapper.selectById(id);
    }

    /**
     * 根据项目ID查询变更请求列表
     */
    public List<ChangeRequest> findByProjectId(Long projectId) {
        LambdaQueryWrapper<ChangeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChangeRequest::getProjectId, projectId);
        wrapper.orderByDesc(ChangeRequest::getCreateTime);
        return changeRequestMapper.selectList(wrapper);
    }
}
