package com.promanage.service.strategy;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.result.PageResult;
import com.promanage.common.entity.User;
import com.promanage.service.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户查询策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class UserQueryStrategy {

    private final UserMapper userMapper;

    /**
     * 分页查询用户
     */
    public PageResult<User> getUsers(int page, int pageSize, String keyword, String status, Long organizationId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or()
                .like(User::getRealName, keyword)
                .or()
                .like(User::getEmail, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }
        
        if (organizationId != null) {
            wrapper.eq(User::getOrganizationId, organizationId);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> pageResult = userMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<User>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据邮箱获取用户
     */
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查邮箱是否存在
     */
    public boolean existsByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取组织用户列表
     */
    public List<User> getUsersByOrganizationId(Long organizationId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOrganizationId, organizationId);
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectList(wrapper);
    }

    /**
     * 获取用户统计信息
     */
    public long getUserCount(Long organizationId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (organizationId != null) {
            wrapper.eq(User::getOrganizationId, organizationId);
        }
        return userMapper.selectCount(wrapper);
    }
}
