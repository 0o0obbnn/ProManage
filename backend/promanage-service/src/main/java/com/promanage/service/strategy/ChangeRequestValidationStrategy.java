package com.promanage.service.strategy;

import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 变更请求验证策略
 * 
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-22
 */
@Component
@RequiredArgsConstructor
public class ChangeRequestValidationStrategy {

    private final IPermissionService permissionService;

    /**
     * 验证变更请求创建参数
     */
    public void validateCreateRequest(ChangeRequest changeRequest, Long currentUserId) {
        // 基础参数验证
        validateBasicParameters(changeRequest);
        
        // 权限验证
        validateCreatePermission(currentUserId, changeRequest.getProjectId());
    }

    /**
     * 验证变更请求更新参数
     */
    public void validateUpdateRequest(ChangeRequest changeRequest, Long currentUserId) {
        // 基础参数验证
        validateBasicParameters(changeRequest);
        
        // 权限验证
        validateUpdatePermission(currentUserId, changeRequest.getProjectId());
    }

    /**
     * 验证基础参数
     */
    private void validateBasicParameters(ChangeRequest changeRequest) {
        if (changeRequest == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求信息不能为空");
        }
        
        if (changeRequest.getTitle() == null || changeRequest.getTitle().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "变更请求标题不能为空");
        }
        
        if (changeRequest.getProjectId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "项目ID不能为空");
        }
    }

    /**
     * 验证创建权限
     */
    private void validateCreatePermission(Long currentUserId, Long projectId) {
        if (!permissionService.isProjectMember(currentUserId, projectId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该项目成员，无权创建变更请求");
        }
    }

    /**
     * 验证更新权限
     */
    private void validateUpdatePermission(Long currentUserId, Long projectId) {
        if (!permissionService.isProjectMember(currentUserId, projectId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不是该项目成员，无权更新变更请求");
        }
    }
}
