package com.promanage.service.strategy;

import com.promanage.common.enums.ChangeRequestStatus;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.ChangeRequestApproval;
import com.promanage.service.mapper.ChangeRequestApprovalMapper;
import com.promanage.service.mapper.ChangeRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 变更请求审批策略
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
@Component
@RequiredArgsConstructor
public class ChangeRequestApprovalStrategy {

    private final ChangeRequestMapper changeRequestMapper;
    private final ChangeRequestApprovalMapper changeRequestApprovalMapper;

    /**
     * 审批变更请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveChangeRequest(Long changeRequestId, String comment, String decision, Long approverId) {
        // 验证变更请求存在
        ChangeRequest changeRequest = validateChangeRequestExists(changeRequestId);
        
        // 验证审批权限
        validateApprovalPermission(changeRequest, approverId);
        
        // 验证审批状态
        validateApprovalStatus(changeRequest);
        
        // 创建审批记录
        createApprovalRecord(changeRequestId, comment, decision, approverId);
        
        // 更新变更请求状态
        updateChangeRequestStatus(changeRequest, decision);
    }

    /**
     * 验证变更请求存在
     */
    private ChangeRequest validateChangeRequestExists(Long changeRequestId) {
        ChangeRequest changeRequest = changeRequestMapper.selectById(changeRequestId);
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "变更请求不存在");
        }
        return changeRequest;
    }

    /**
     * 验证审批权限
     */
    private void validateApprovalPermission(ChangeRequest changeRequest, Long approverId) {
        // 检查是否是项目管理员或审批人
        if (!isProjectAdmin(approverId, changeRequest.getProjectId()) && 
            !approverId.equals(changeRequest.getApproverId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您无权审批此变更请求");
        }
    }

    /**
     * 验证审批状态
     */
    private void validateApprovalStatus(ChangeRequest changeRequest) {
        if (changeRequest.getStatus() != ChangeRequestStatus.PENDING_APPROVAL.getCode()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "变更请求状态不允许审批");
        }
    }

    /**
     * 创建审批记录
     */
    private void createApprovalRecord(Long changeRequestId, String comment, String decision, Long approverId) {
        ChangeRequestApproval approval = new ChangeRequestApproval();
        approval.setChangeRequestId(changeRequestId);
        approval.setApproverId(approverId);
        approval.setDecision(decision);
        approval.setComment(comment);
        approval.setApprovalTime(LocalDateTime.now());
        
        changeRequestApprovalMapper.insert(approval);
    }

    /**
     * 更新变更请求状态
     */
    private void updateChangeRequestStatus(ChangeRequest changeRequest, String decision) {
        ChangeRequestStatus newStatus = "APPROVED".equals(decision) ? 
            ChangeRequestStatus.APPROVED : ChangeRequestStatus.REJECTED;
        
        changeRequest.setStatus(newStatus.getCode());
        changeRequest.setUpdateTime(LocalDateTime.now());
        
        changeRequestMapper.updateById(changeRequest);
    }

    /**
     * 检查是否是项目管理员
     */
    private boolean isProjectAdmin(Long userId, Long projectId) {
        // TODO: 实现项目管理员权限检查
        return false;
    }

    /**
     * 获取变更请求审批历史
     */
    public List<ChangeRequestApproval> getApprovalHistory(Long changeRequestId) {
        return changeRequestApprovalMapper.selectByChangeRequestId(changeRequestId);
    }
}
