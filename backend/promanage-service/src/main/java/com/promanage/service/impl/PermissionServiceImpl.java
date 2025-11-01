package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.Notification;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.entity.Task;
import com.promanage.domain.entity.UserRole;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.NotificationMapper;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.UserRoleMapper;
import com.promanage.service.service.IPermissionService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限校验服务实现类
 *
 * <p>用于集中处理整个应用中的权限检查逻辑，防止业务逻辑中的权限漏洞。
 *
 * @author ProManage Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private static final Set<String> SUPER_ADMIN_ROLE_CODES = Set.of(
        "ROLE_SUPER_ADMIN",
        "SUPER_ADMIN",
        "SYSTEM_ADMIN"
    );

    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final TaskMapper taskMapper;
    private final DocumentMapper documentMapper;
    private final NotificationMapper notificationMapper;
    private final ChangeRequestMapper changeRequestMapper;

    @Override
    public boolean isOrganizationMember(Long userId, Long organizationId) {
        log.debug(
            "检查用户是否为组织成员, userId={}, organizationId={}",
            userId,
            organizationId
        );

        // 超级管理员自动是所有组织成员
        if (isSuperAdmin(userId)) {
            return true;
        }

        // 检查用户是否属于指定组织
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        return organizationId.equals(user.getOrganizationId());
    }

    @Override
    public boolean isProjectMember(Long userId, Long projectId) {
        log.debug(
            "检查用户是否为项目成员, userId={}, projectId={}",
            userId,
            projectId
        );

        // 超级管理员自动是所有项目成员
        if (isSuperAdmin(userId)) {
            return true;
        }

        // 检查用户是否为项目成员
        LambdaQueryWrapper<ProjectMember> queryWrapper =
            new LambdaQueryWrapper<>();
        queryWrapper
            .eq(ProjectMember::getUserId, userId)
            .eq(ProjectMember::getProjectId, projectId)
            .eq(ProjectMember::getStatus, 1); // 状态正常

        return projectMemberMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean isProjectAdmin(Long userId, Long projectId) {
        log.debug(
            "检查用户是否为项目管理员, userId={}, projectId={}",
            userId,
            projectId
        );

        // 超级管理员自动是所有项目管理员
        if (isSuperAdmin(userId)) {
            return true;
        }

        // 检查用户是否为项目管理员
        LambdaQueryWrapper<ProjectMember> queryWrapper =
            new LambdaQueryWrapper<>();
        queryWrapper
            .eq(ProjectMember::getUserId, userId)
            .eq(ProjectMember::getProjectId, projectId)
            .eq(ProjectMember::getRoleId, 1L) // 项目管理员角色ID
            .eq(ProjectMember::getStatus, 1); // 状态正常

        return projectMemberMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean canAccessTask(Long userId, Long taskId) {
        log.debug(
            "检查用户是否可以访问任务, userId={}, taskId={}",
            userId,
            taskId
        );

        // 获取任务信息
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted()) {
            throw new BusinessException(
                ResultCode.DATA_NOT_FOUND,
                "任务不存在"
            );
        }

        // 检查用户是否为任务所在项目的成员
        return isProjectMember(userId, task.getProjectId());
    }

    @Override
    public boolean canAccessDocument(Long userId, Long documentId) {
        log.debug(
            "检查用户是否可以访问文档, userId={}, documentId={}",
            userId,
            documentId
        );

        // 获取文档信息
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted()) {
            log.debug("文档不存在或已删除, documentId={}", documentId);
            return false;
        }

        // 检查用户是否为文档所在项目的成员
        return isProjectMember(userId, document.getProjectId());
    }

    @Override
    public boolean canAccessNotification(Long userId, Long notificationId) {
        log.debug(
            "检查用户是否可以访问通知, userId={}, notificationId={}",
            userId,
            notificationId
        );

        // 获取通知信息
        Notification notification = notificationMapper.selectById(
            notificationId
        );
        if (notification == null || notification.getDeleted()) {
            throw new BusinessException(
                ResultCode.DATA_NOT_FOUND,
                "通知不存在"
            );
        }

        // 检查用户是否为通知接收者或发送者
        return (
            userId.equals(notification.getUserId()) ||
            userId.equals(notification.getCreatorId())
        );
    }

    @Override
    public boolean isOrganizationAdmin(Long userId, Long organizationId) {
        log.debug(
            "检查用户是否为组织管理员, userId={}, organizationId={}",
            userId,
            organizationId
        );

        // 超级管理员自动是所有组织管理员
        if (isSuperAdmin(userId)) {
            return true;
        }

        // 检查用户是否为组织所有者
        Organization organization = organizationMapper.selectById(
            organizationId
        );
        if (organization != null) {
            // 检查用户是否为组织的创建者或其他管理员标识
            // 这里我们假设组织的创建者ID存储在creatorId字段中
            if (userId.equals(organization.getCreatorId())) {
                return true;
            }
        }

        // 检查用户是否具有组织管理员角色
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
            .eq(UserRole::getUserId, userId)
            .eq(UserRole::getRoleId, 2L); // 组织管理员角色ID

        return userRoleMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean canAccessChangeRequest(Long userId, Long changeRequestId) {
        log.debug(
            "检查用户是否可以访问变更请求, userId={}, changeRequestId={}",
            userId,
            changeRequestId
        );

        // 获取变更请求信息
        ChangeRequest changeRequest = changeRequestMapper.selectById(
            changeRequestId
        );
        if (changeRequest == null || changeRequest.getDeleted()) {
            throw new BusinessException(
                ResultCode.DATA_NOT_FOUND,
                "变更请求不存在"
            );
        }

        // 检查用户是否为变更请求所在项目的成员
        return isProjectMember(userId, changeRequest.getProjectId());
    }

    @Override
    public boolean canApproveChangeRequest(Long userId, Long changeRequestId) {
        log.debug(
            "检查用户是否可以审批变更请求, userId={}, changeRequestId={}",
            userId,
            changeRequestId
        );

        // 获取变更请求信息
        ChangeRequest changeRequest = changeRequestMapper.selectById(
            changeRequestId
        );
        if (changeRequest == null || changeRequest.getDeleted()) {
            throw new BusinessException(
                ResultCode.DATA_NOT_FOUND,
                "变更请求不存在"
            );
        }

        // 检查用户是否为变更请求所在项目的管理员
        return isProjectAdmin(userId, changeRequest.getProjectId());
    }

    @Override
    public boolean canModifyUser(Long actorId, Long targetUserId) {
        log.debug(
            "检查用户是否可以修改用户信息, actorId={}, targetUserId={}",
            actorId,
            targetUserId
        );

        // 用户只能修改自己的信息，除非是超级管理员
        return actorId.equals(targetUserId) || isSuperAdmin(actorId);
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        log.debug("检查用户是否为超级管理员, userId={}", userId);

        // 获取用户角色编码
        List<String> roleCodes = userRoleMapper.selectRoleCodesByUserId(userId);

        // 检查是否包含超级管理员角色编码
        return roleCodes.stream().anyMatch(SUPER_ADMIN_ROLE_CODES::contains);
    }
}
