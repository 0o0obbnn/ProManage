package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.constant.UserConstants;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户资料策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileStrategy {

    private final UserMapper userMapper;

    /**
     * 更新用户基本信息
     */
    public void updateBasicInfo(Long userId, String realName, String email, String phone, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }
        
        if (realName != null) {
            user.setRealName(realName);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 更新用户头像
     */
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }
        
        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }

        // Convert String status to Integer (0-disabled, 1-enabled, 2-locked)
        Integer statusCode;
        switch (status.toUpperCase()) {
            case "DISABLED":
                statusCode = 0;
                break;
            case "ENABLED":
            case "ACTIVE":
                statusCode = 1;
                break;
            case "LOCKED":
                statusCode = 2;
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "Invalid status: " + status);
        }
        user.setStatus(statusCode);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 批量更新用户状态
     */
    public void batchUpdateStatus(List<Long> userIds, String status) {
        for (Long userId : userIds) {
            updateStatus(userId, status);
        }
    }

    /**
     * 更新用户组织
     */
    public void updateOrganization(Long userId, Long organizationId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }
        
        user.setOrganizationId(organizationId);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 清除用户缓存
     */
    public void clearUserCache(Long userId) {
        // 这里应该清除用户相关的缓存
        // 具体实现取决于使用的缓存框架
        log.info("清除用户缓存, userId={}", userId);
    }

    /**
     * 获取用户完整信息
     */
    public User getUserFullInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }
        return user;
    }
}
