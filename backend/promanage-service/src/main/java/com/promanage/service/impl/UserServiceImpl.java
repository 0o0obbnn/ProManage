package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
import com.promanage.domain.mapper.PermissionMapper;
import com.promanage.domain.mapper.RoleMapper;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.constant.UserConstants;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.service.IUserPermissionService;
import com.promanage.service.service.IUserService;
import com.promanage.service.strategy.UserAuthStrategy;
import com.promanage.service.strategy.UserProfileStrategy;
import com.promanage.service.strategy.UserQueryStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 * 使用策略模式减少方法数量
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final IUserPermissionService userPermissionService;

    // 策略类
    private final UserQueryStrategy queryStrategy;
    private final UserAuthStrategy authStrategy;
    private final UserProfileStrategy profileStrategy;

    // ==================== 查询操作 ====================

    @Override
    public User getById(Long id) {
        log.debug("查询用户, id={}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public Map<Long, User> getByIds(List<Long> ids) {
        log.debug("批量查询用户, ids={}", ids);
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        List<User> users = userMapper.selectBatchIds(ids);
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    @Override
    public User getByUsername(String username) {
        log.debug("根据用户名查询用户, username={}", username);
        return queryStrategy.getUserByUsername(username);
    }

    @Override
    public User getByEmail(String email) {
        log.debug("根据邮箱查询用户, email={}", email);
        return queryStrategy.getUserByEmail(email);
    }

    @Override
    public PageResult<User> listUsers(Integer page, Integer pageSize, String keyword) {
        log.debug("分页查询用户列表, page={}, pageSize={}, keyword={}", page, pageSize, keyword);
        return queryStrategy.getUsers(page, pageSize, keyword, null, null);
    }

    @Override
    public List<User> listAll() {
        log.debug("查询所有用户");
        return userMapper.selectList(null);
    }

    @Override
    public long countByOrganizationId(Long organizationId) {
        log.debug("统计组织用户数量, organizationId={}", organizationId);
        return queryStrategy.getUserCount(organizationId);
    }

    @Override
    public List<User> listByOrganizationId(Long organizationId) {
        log.debug("查询组织用户列表, organizationId={}", organizationId);
        return queryStrategy.getUsersByOrganizationId(organizationId);
    }

    @Override
    public PageResult<User> listByOrganizationId(Long organizationId, Integer page, Integer pageSize) {
        log.debug("分页查询组织用户列表, organizationId={}, page={}, pageSize={}", organizationId, page, pageSize);
        return queryStrategy.getUsers(page, pageSize, null, null, organizationId);
    }

    // ==================== 创建操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(User user) {
        log.info("创建用户, username={}", user.getUsername());
        validateUser(user);

        // 检查用户名唯一性
        if (existsByUsername(user.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        // 检查邮箱唯一性
        if (user.getEmail() != null && existsByEmail(user.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 设置默认状态
        if (user.getStatus() == null) {
            user.setStatus(0); // 正常状态
        }

        userMapper.insert(user);
        log.info("创建用户成功, id={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(User user) {
        log.info("用户注册, username={}", user.getUsername());
        Long userId = create(user);

        // TODO: 分配默认角色
        // 可以从配置中读取默认角色ID并分配

        return userId;
    }

    // ==================== 更新操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, User user) {
        log.info("更新用户信息, id={}", id);

        // 检查用户是否存在
        User existingUser = getById(id);

        // 更新字段
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            // 检查新邮箱是否已被使用
            if (existsByEmail(user.getEmail())) {
                throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
            }
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getDepartmentId() != null) {
            existingUser.setDepartmentId(user.getDepartmentId());
        }
        if (user.getPosition() != null) {
            existingUser.setPosition(user.getPosition());
        }

        userMapper.updateById(existingUser);
        log.info("更新用户信息成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long id, String oldPassword, String newPassword, String confirmPassword) {
        log.info("修改用户密码, id={}", id);

        // 验证新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 获取用户
        User user = getById(id);

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.WRONG_PASSWORD);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("修改用户密码成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        log.info("重置用户密码, id={}", id);

        User user = getById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("重置用户密码成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        log.info("更新用户状态, id={}, status={}", id, status);

        User user = getById(id);
        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态成功, id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLogin(Long id, String ip) {
        log.debug("更新用户最后登录信息, id={}, ip={}", id, ip);

        User user = getById(id);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
    }

    // ==================== 删除操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("删除用户, id={}", id);

        // 检查用户是否存在
        getById(id);

        // 逻辑删除
        userMapper.deleteById(id);

        log.info("删除用户成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids) {
        log.info("批量删除用户, ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int count = userMapper.deleteBatchIds(ids);
        log.info("批量删除用户成功, count={}", count);
        return count;
    }

    // ==================== 角色管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色, userId={}, roleIds={}", userId, roleIds);

        // 检查用户是否存在
        getById(userId);

        // 使用权限服务分配角色
        userPermissionService.assignRolesToUser(userId, roleIds);

        log.info("为用户分配角色成功, userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(Long userId, Long roleId) {
        log.info("为用户添加角色, userId={}, roleId={}", userId, roleId);

        // 检查用户是否存在
        getById(userId);

        // 获取现有角色
        List<Long> roleIds = userPermissionService.getUserRoleIds(userId).stream().toList();

        // 添加新角色
        if (!roleIds.contains(roleId)) {
            roleIds.add(roleId);
            userPermissionService.assignRolesToUser(userId, roleIds);
        }

        log.info("为用户添加角色成功, userId={}, roleId={}", userId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long userId, Long roleId) {
        log.info("移除用户角色, userId={}, roleId={}", userId, roleId);

        // 检查用户是否存在
        getById(userId);

        // 获取现有角色
        List<Long> roleIds = userPermissionService.getUserRoleIds(userId).stream()
                .filter(id -> !id.equals(roleId))
                .toList();

        // 重新分配角色
        userPermissionService.assignRolesToUser(userId, roleIds);

        log.info("移除用户角色成功, userId={}, roleId={}", userId, roleId);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        log.debug("查询用户角色, userId={}", userId);

        // 检查用户是否存在
        getById(userId);

        // 通过RoleMapper查询用户角色
        List<Role> roles = roleMapper.findByUserId(userId);

        log.debug("查询用户角色成功, userId={}, roleCount={}", userId, roles.size());
        return roles;
    }

    // ==================== 权限检查 ====================

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        log.debug("查询用户权限, userId={}", userId);

        // 检查用户是否存在
        getById(userId);

        // 通过PermissionMapper查询用户权限
        List<Permission> permissions = permissionMapper.findByUserId(userId);

        log.debug("查询用户权限成功, userId={}, permissionCount={}", userId, permissions.size());
        return permissions;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        log.debug("检查用户权限, userId={}, permissionCode={}", userId, permissionCode);

        // 获取用户所有权限
        List<Permission> permissions = getUserPermissions(userId);

        // 检查是否包含指定权限
        boolean hasPermission = permissions.stream()
                .anyMatch(p -> permissionCode.equals(p.getPermissionCode()));

        log.debug("权限检查结果, userId={}, permissionCode={}, hasPermission={}",
                userId, permissionCode, hasPermission);
        return hasPermission;
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        log.debug("检查用户角色, userId={}, roleCode={}", userId, roleCode);

        List<String> roleCodes = userPermissionService.getUserRoleCodes(userId);
        return roleCodes.contains(roleCode);
    }

    // ==================== 验证操作 ====================

    @Override
    public boolean verifyPassword(Long userId, String password) {
        log.debug("验证用户密码, userId={}", userId);

        User user = getById(userId);
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryStrategy.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return queryStrategy.existsByEmail(email);
    }

    // ==================== 私有辅助方法 ====================

    private void validateUser(User user) {
        if (user == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户对象不能为空");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
        }
    }
}
