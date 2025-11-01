package com.promanage.service.impl;

import com.promanage.common.domain.BatchOperationResult;
import com.promanage.common.enums.ChangeRequestStatus;
import com.promanage.common.enums.Priority;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.constant.ChangeRequestConstants;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.service.IChangeRequestService;
import com.promanage.service.strategy.ChangeRequestApprovalStrategy;
import com.promanage.service.strategy.ChangeRequestQueryStrategy;
import com.promanage.service.strategy.ChangeRequestValidationStrategy;
import com.promanage.service.dto.request.ChangeRequestQueryRequest;
import org.springframework.dao.DataAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 变更请求服务实现类（重构版）
 * 
 * <p>使用策略模式降低复杂度，提高可维护性
 *
 * @author ProManage Team
 * @version 2.0
 * @since 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeRequestServiceImpl implements IChangeRequestService {

    private final ChangeRequestMapper changeRequestMapper;
    private final ChangeRequestValidationStrategy validationStrategy;
    private final ChangeRequestQueryStrategy queryStrategy;
    private final ChangeRequestApprovalStrategy approvalStrategy;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChangeRequest(ChangeRequest changeRequest) {
        log.info("创建变更请求, title={}, projectId={}", 
                changeRequest != null ? changeRequest.getTitle() : null,
                changeRequest != null ? changeRequest.getProjectId() : null);

        // 获取当前用户
        Long currentUserId = getCurrentUserId();
        
        // 验证参数和权限
        validationStrategy.validateCreateRequest(changeRequest, currentUserId);
        
        // 设置默认值和创建信息
        setupChangeRequestDefaults(changeRequest, currentUserId);
        
        // 保存变更请求
        int result = changeRequestMapper.insert(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "创建变更请求失败");
        }
        
        log.info("变更请求创建成功, id={}", changeRequest.getId());
        return changeRequest.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChangeRequest(ChangeRequest changeRequest) {
        log.info("更新变更请求, id={}", changeRequest.getId());
        
        // 获取当前用户
        Long currentUserId = getCurrentUserId();
        
        // 验证参数和权限
        validationStrategy.validateUpdateRequest(changeRequest, currentUserId);
        
        // 验证变更请求存在
        ChangeRequest existingRequest = queryStrategy.findById(changeRequest.getId());
        if (existingRequest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ChangeRequestConstants.ERROR_CHANGE_REQUEST_NOT_FOUND);
        }
        
        // 设置更新信息
        changeRequest.setUpdaterId(currentUserId);
        changeRequest.setUpdateTime(LocalDateTime.now());
        
        // 更新变更请求
        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新变更请求失败");
        }
        
        log.info("变更请求更新成功, id={}", changeRequest.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChangeRequest(Long changeRequestId, Long userId) {
        log.info("删除变更请求, id={}, userId={}", changeRequestId, userId);
        
        // 验证变更请求存在
        ChangeRequest changeRequest = queryStrategy.findById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ChangeRequestConstants.ERROR_CHANGE_REQUEST_NOT_FOUND);
        }
        
        // 验证删除权限
        validateDeletePermission(changeRequest, userId);
        
        // 软删除变更请求
        changeRequest.setDeleted(true);
        changeRequest.setUpdateTime(LocalDateTime.now());
        changeRequest.setUpdaterId(userId);
        
        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "删除变更请求失败");
        }
        
        log.info("变更请求删除成功, id={}", changeRequestId);
    }

    @Override
    public PageResult<ChangeRequest> listChangeRequests(Long projectId, Integer page, Integer pageSize, 
                                                      String keyword, Integer status, String priority, 
                                                      Long requesterId, Long assigneeId, Long approverId, 
                                                      String startDate, String endDate) {
        log.info("查询变更请求列表, projectId={}, page={}, pageSize={}", projectId, page, pageSize);
        
        // 使用DTO对象封装查询参数
        ChangeRequestQueryRequest queryRequest = ChangeRequestQueryRequest.builder()
            .projectId(projectId)
            .page(page)
            .pageSize(pageSize)
            .keyword(keyword)
            .status(status)
            .priority(priority)
            .requesterId(requesterId)
            .assigneeId(assigneeId)
            .approverId(approverId)
            .startDate(startDate)
            .endDate(endDate)
            .build();
        
        return queryStrategy.queryChangeRequests(queryRequest);
    }

    @Override
    public ChangeRequest getChangeRequestById(Long id) {
        log.info("查询变更请求详情, id={}", id);
        
        ChangeRequest changeRequest = queryStrategy.findById(id);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ChangeRequestConstants.ERROR_CHANGE_REQUEST_NOT_FOUND);
        }
        
        return changeRequest;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveChangeRequest(Long changeRequestId, String comment, String decision, Long approverId) {
        log.info("审批变更请求, id={}, decision={}, approverId={}", changeRequestId, decision, approverId);
        
        approvalStrategy.approveChangeRequest(changeRequestId, comment, decision, approverId);
        
        log.info("变更请求审批完成, id={}, decision={}", changeRequestId, decision);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitChangeRequest(Long changeRequestId, Long userId) {
        log.info("提交变更请求, id={}, userId={}", changeRequestId, userId);
        
        ChangeRequest changeRequest = queryStrategy.findById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ChangeRequestConstants.ERROR_CHANGE_REQUEST_NOT_FOUND);
        }
        
        // 验证状态
        // 仅允许草稿提交
        if (!ChangeRequestStatus.DRAFT.getCode().equalsIgnoreCase(String.valueOf(changeRequest.getStatus()))) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, ChangeRequestConstants.ERROR_INVALID_STATUS);
        }
        
        // 更新状态
        // 提交后状态置为 SUBMITTED
        changeRequest.setStatus(ChangeRequestStatus.SUBMITTED.getCode());
        changeRequest.setUpdateTime(LocalDateTime.now());
        changeRequest.setUpdaterId(userId);
        
        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "提交变更请求失败");
        }
        
        log.info("变更请求提交成功, id={}", changeRequestId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeChangeRequest(Long changeRequestId, Long userId) {
        log.info("关闭变更请求, id={}, userId={}", changeRequestId, userId);
        
        updateChangeRequestStatus(changeRequestId, ChangeRequestStatus.CLOSED, userId);
        
        log.info("变更请求关闭成功, id={}", changeRequestId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reopenChangeRequest(Long changeRequestId, Long userId) {
        log.info("重新打开变更请求, id={}, userId={}", changeRequestId, userId);
        
        updateChangeRequestStatus(changeRequestId, ChangeRequestStatus.DRAFT, userId);
        
        log.info("变更请求重新打开成功, id={}", changeRequestId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResult<Long> batchUpdateChangeRequestStatus(List<Long> changeRequestIds, String status, Long userId) {
        log.info("批量更新变更请求状态, ids={}, status={}, userId={}", changeRequestIds, status, userId);
        
        BatchOperationResult<Long> result = new BatchOperationResult<>();
        
        for (Long changeRequestId : changeRequestIds) {
            try {
                updateChangeRequestStatus(changeRequestId, ChangeRequestStatus.valueOf(status), userId);
                result.addSuccess(changeRequestId);
            } catch (DataAccessException e) {
                log.error("批量更新变更请求状态失败, id={}", changeRequestId, e);
                result.addFailure(changeRequestId, e.getMessage());
            }
        }
        
        log.info("批量更新变更请求状态完成, 成功={}, 失败={}", 
                result.getSuccessCount(), result.getFailureCount());
        
        return result;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, ChangeRequestConstants.ERROR_USER_NOT_LOGIN));
    }

    /**
     * 设置变更请求默认值
     */
    private void setupChangeRequestDefaults(ChangeRequest changeRequest, Long currentUserId) {
        if (changeRequest.getStatus() == null) {
            changeRequest.setStatus(ChangeRequestStatus.DRAFT.getCode());
        }
        if (changeRequest.getPriority() == null) {
            changeRequest.setPriority(Priority.MEDIUM.getValue());
        }
        
        changeRequest.setRequesterId(currentUserId);
        changeRequest.setCreatorId(currentUserId);
        changeRequest.setUpdaterId(currentUserId);
        changeRequest.setCreateTime(LocalDateTime.now());
        changeRequest.setUpdateTime(LocalDateTime.now());
    }

    /**
     * 验证删除权限
     */
    private void validateDeletePermission(ChangeRequest changeRequest, Long userId) {
        // 只有创建者或项目管理员可以删除
        if (!changeRequest.getCreatorId().equals(userId)) {
            // TODO: 检查项目管理员权限
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权删除此变更请求");
        }
    }

    /**
     * 更新变更请求状态
     */
    private void updateChangeRequestStatus(Long changeRequestId, ChangeRequestStatus status, Long userId) {
        ChangeRequest changeRequest = queryStrategy.findById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, ChangeRequestConstants.ERROR_CHANGE_REQUEST_NOT_FOUND);
        }
        
        changeRequest.setStatus(status.getCode());
        changeRequest.setUpdateTime(LocalDateTime.now());
        changeRequest.setUpdaterId(userId);
        
        int result = changeRequestMapper.updateById(changeRequest);
        if (result <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "更新变更请求状态失败");
        }
    }

    // ==================== 接口补全实现（基础占位实现，可后续完善） ====================

    @Override
    public java.util.List<com.promanage.service.entity.ChangeRequestImpact> analyzeChangeRequestImpact(Long changeRequestId, boolean forceRefresh) {
        // TODO: 后续接入影响分析策略
        return java.util.Collections.emptyList();
    }

    @Override
    public java.util.List<com.promanage.service.entity.ChangeRequestImpact> getChangeRequestImpacts(Long changeRequestId) {
        // TODO: 查询影响分析结果
        return java.util.Collections.emptyList();
    }

    @Override
    public void validateImpactAnalysis(Long impactId, boolean isValid, Long userId) {
        // TODO: 标记某条影响为已验证/无效
    }

    @Override
    public PageResult<ChangeRequest> listChangeRequestsByUser(Long userId, Integer page, Integer size, String status) {
        // 复用查询策略
        com.promanage.service.dto.request.ChangeRequestQueryRequest req = com.promanage.service.dto.request.ChangeRequestQueryRequest
            .builder()
            .requesterId(userId)
            .page(page)
            .pageSize(size)
            .status(status == null ? null : null)
            .build();
        return queryStrategy.queryChangeRequests(req);
    }

    @Override
    public PageResult<ChangeRequest> listPendingApprovalChangeRequests(Long reviewerId, Integer page, Integer size) {
        // 复用查询策略，按审批人过滤 + 状态为 UNDER_REVIEW
        com.promanage.service.dto.request.ChangeRequestQueryRequest req = com.promanage.service.dto.request.ChangeRequestQueryRequest
            .builder()
            .approverId(reviewerId)
            .page(page)
            .pageSize(size)
            .build();
        return queryStrategy.queryChangeRequests(req);
    }

    @Override
    public int countChangeRequestsByProject(Long projectId) {
        // 简化实现：查询列表计数
        return queryStrategy.findByProjectId(projectId).size();
    }

    @Override
    public int countChangeRequestsByUser(Long userId, String status) {
        // 简化实现：按请求人过滤后计数
        com.promanage.service.dto.request.ChangeRequestQueryRequest req = com.promanage.service.dto.request.ChangeRequestQueryRequest
            .builder()
            .requesterId(userId)
            .build();
        Long total = queryStrategy.queryChangeRequests(req).getTotal();
        return total == null ? 0 : total.intValue();
    }

    @Override
    public int countPendingApprovalChangeRequests(Long reviewerId) {
        // 简化实现：复用待审列表
        Long total = listPendingApprovalChangeRequests(reviewerId, 1, 1).getTotal();
        return total == null ? 0 : total.intValue();
    }

    @Override
    public boolean hasChangeRequestPermission(Long changeRequestId, Long userId) {
        // 简化校验：创建者即可；可扩展为项目角色校验
        ChangeRequest cr = queryStrategy.findById(changeRequestId);
        return cr != null && (userId != null && userId.equals(cr.getCreatorId()));
    }

    @Override
    public boolean hasChangeRequestViewPermission(Long changeRequestId, Long userId) {
        // 简化校验：创建者可查看
        return hasChangeRequestPermission(changeRequestId, userId);
    }

    @Override
    public boolean canApproveChangeRequest(Long changeRequestId, Long userId) {
        // 简化：仅占位，后续接入审批人策略
        return false;
    }

    @Override
    public void implementChangeRequest(Long changeRequestId, Long userId) {
        // 简化：将状态置为 IMPLEMENTED
        updateChangeRequestStatus(changeRequestId, ChangeRequestStatus.IMPLEMENTED, userId);
    }

    @Override
    public java.util.List<com.promanage.service.entity.ChangeRequestApproval> getChangeRequestApprovalHistory(Long changeRequestId) {
        // TODO: 调用审批策略/Mapper 查询
        return java.util.Collections.emptyList();
    }

    @Override
    public int getChangeRequestCommentCount(Long changeRequestId) {
        // TODO: 评论表接入后实现
        return 0;
    }

    @Override
    public int getChangeRequestImpactCount(Long changeRequestId) {
        // TODO: 根据影响表统计
        return 0;
    }

    @Override
    public ChangeRequestStatistics getChangeRequestStatistics(Long projectId) {
        // 简化占位实现
        return new ChangeRequestStatistics();
    }
}
