package com.promanage.service.strategy;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.promanage.common.exception.BusinessException;
import com.promanage.common.domain.ResultCode;
import com.promanage.service.constant.UserConstants;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户认证策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthStrategy {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 验证用户密码
     */
    public boolean validatePassword(User user, String rawPassword) {
        if (user == null || rawPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 更新用户密码
     */
    public void updatePassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }
        
        user.setPassword(encodePassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 重置用户密码
     */
    public void resetPassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, UserConstants.ERROR_USER_NOT_FOUND);
        }
        
        user.setPassword(encodePassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 更新最后登录信息
     */
    public void updateLastLoginInfo(Long userId, String loginIp) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(loginIp);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    /**
     * 锁定用户
     */
    public void lockUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(2);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    /**
     * 解锁用户
     */
    public void unlockUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(1);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    private User getUserByEmail(String email) {
        // 这里应该调用UserQueryStrategy，但为了避免循环依赖，直接实现
        return userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
    }
}
