package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.config.UserProperties;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.Permission;
import com.promanage.service.entity.Role;
import com.promanage.common.entity.User;
import com.promanage.service.entity.UserRole;
import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.RoleMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.UserRoleMapper;
import com.promanage.service.service.IPasswordService;
import com.promanage.service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * <p>
 * 实现用户管理的所有业务逻辑,包括CRUD操作、角色管理和权限检查
 * 使用Redis缓存提高查询性能,使用事务保证数据一致性
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserProperties userProperties;
    private final CacheManager cacheManager;
    private final IPasswordService passwordService;

    @Override
    @Cacheable(value = "users:id", key = "#id", unless = "#result == null")
    public User getById(Long id) {
        log.info("查询用户详情, id={}", id);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }

        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted()) {
            log.warn("用户不存在, id={}", id);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return user;
    }

    @Override
    public Map<Long, User> getByIds(List<Long> ids) {
        log.info("批量查询用户, ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        // 去重
        List<Long> uniqueIds = ids.stream()
            .distinct()
            .filter(id -> id != null)
            .collect(Collectors.toList());

        if (uniqueIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 使用MyBatis-Plus的selectByIds方法批量查询
        List<User> users = userMapper.selectByIds(uniqueIds);

        // 转换为Map，方便按ID查找
        Map<Long, User> userMap = users.stream()
            .filter(user -> user != null && !user.getDeleted())
            .collect(Collectors.toMap(User::getId, user -> user));

        log.info("批量查询用户完成, 请求数量={}, 查询到数量={}", uniqueIds.size(), userMap.size());
        return userMap;
    }

    @Override
    @Cacheable(value = "users:username", key = "#username", unless = "#result == null")
    public User getByUsername(String username) {
        log.info("根据用户名查询用户, username={}", username);

        if (StringUtils.isBlank(username)) {
            return null;
        }

        return userMapper.findByUsername(username);
    }

    @Override
    @Cacheable(value = "users:email", key = "#email", unless = "#result == null")
    public User getByEmail(String email) {
        log.info("根据邮箱查询用户, email={}", email);

        if (StringUtils.isBlank(email)) {
            return null;
        }

        return userMapper.findByEmail(email);
    }

    @Override
    public PageResult<User> listUsers(Integer page, Integer pageSize, String keyword) {
        log.info("分页查询用户列表, page={}, pageSize={}, keyword={}", page, pageSize, keyword);

        // 构建分页对象
        Page<User> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(w -> w
                .like(User::getUsername, keyword)
                .or().like(User::getEmail, keyword)
                .or().like(User::getRealName, keyword)
            );
        }
        queryWrapper.orderByDesc(User::getCreateTime);

        // 执行查询
        IPage<User> result = userMapper.selectPage(pageParam, queryWrapper);

        log.info("查询用户列表成功, total={}", result.getTotal());
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    @Override
    public List<User> listAll() {
        log.info("查询所有用户");
        return userMapper.selectList(new LambdaQueryWrapper<User>()
            .orderByDesc(User::getCreateTime));
    }

    @Override
    public long countByOrganizationId(Long organizationId) {
        log.debug("统计组织成员数量, organizationId={}", organizationId);
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
            .eq(User::getOrganizationId, organizationId)
            .isNull(User::getDeletedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(User user) {
        log.info("创建用户, username={}", user.getUsername());

        // 参数验证
        validateUser(user, true);

        // 检查用户名唯一性
        if (existsByUsername(user.getUsername())) {
            log.warn("用户名已存在, username={}", user.getUsername());
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱唯一性
        if (StringUtils.isNotBlank(user.getEmail()) && existsByEmail(user.getEmail())) {
            log.warn("邮箱已存在, email={}", user.getEmail());
            throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱已被使用");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(0); // 默认正常状态
        }

        // 保存用户
        userMapper.insert(user);

        log.info("创建用户成功, id={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(User user) {
        log.info("用户注册, username={}", user.getUsername());

        // 创建用户
        Long userId = create(user);

        // 分配默认角色
        Long defaultRoleId = userProperties.getDefaultRoleId();
        addRole(userId, defaultRoleId);

        log.info("用户注册成功, userId={}", userId);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, User user) {
        log.info("更新用户信息, id={}", id);

        // 检查用户是否存在
        User existingUser = getById(id);

        // 记录旧的用户名和邮箱，用于清除缓存
        String oldUsername = existingUser.getUsername();
        String oldEmail = existingUser.getEmail();

        // 更新字段
        if (StringUtils.isNotBlank(user.getEmail())) {
            // 检查邮箱唯一性
            User emailUser = getByEmail(user.getEmail());
            if (emailUser != null && !emailUser.getId().equals(id)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "邮箱已被使用");
            }
            existingUser.setEmail(user.getEmail());
        }
        if (StringUtils.isNotBlank(user.getPhone())) {
            existingUser.setPhone(user.getPhone());
        }
        if (StringUtils.isNotBlank(user.getRealName())) {
            existingUser.setRealName(user.getRealName());
        }
        if (StringUtils.isNotBlank(user.getAvatar())) {
            existingUser.setAvatar(user.getAvatar());
        }

        // 保存更新
        userMapper.updateById(existingUser);

        // 手动清除缓存
        evictUserCache(id, oldUsername, oldEmail);
        // 如果邮箱或用户名发生变化，也清除新的缓存键
        if (user.getEmail() != null && !user.getEmail().equals(oldEmail)) {
            evictCacheByKey("users:email", user.getEmail());
        }

        log.info("更新用户信息成功, id={}", id);
    }

    /**
     * 清除用户相关缓存
     */
    private void evictUserCache(Long userId, String username, String email) {
        evictCacheByKey("users:id", userId);
        if (username != null) {
            evictCacheByKey("users:username", username);
        }
        if (email != null) {
            evictCacheByKey("users:email", email);
        }
        evictCacheByKey("userRoles", userId);
        evictCacheByKey("userPermissions", userId);
    }

    /**
     * 清除指定缓存键
     */
    private void evictCacheByKey(String cacheName, Object key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                log.debug("清除缓存: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.warn("清除缓存失败: cacheName={}, key={}, error={}", cacheName, key, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users:id", key = "#id")
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        log.info("修改密码, userId={}", id);

        // 参数验证
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能为空");
        }

        // 检查用户是否存在
        User user = getById(id);

        // 验证旧密码
        if (!verifyPassword(id, oldPassword)) {
            log.warn("旧密码错误, userId={}", id);
            throw new BusinessException(ResultCode.PARAM_ERROR, "旧密码错误");
        }

        // 加密新密码并保存
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("修改密码成功, userId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users:id", key = "#id")
    public void resetPassword(Long id, String newPassword) {
        log.info("重置密码, userId={}", id);

        // 参数验证
        if (StringUtils.isBlank(newPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能为空");
        }

        // 检查用户是否存在
        User user = getById(id);

        // 加密新密码并保存
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("重置密码成功, userId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users:id", key = "#id")
    public void updateStatus(Long id, Integer status) {
        log.info("更新用户状态, userId={}, status={}", id, status);

        // 参数验证
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "状态值无效");
        }

        // 检查用户是否存在
        User user = getById(id);

        // 更新状态
        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态成功, userId={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "users:id", key = "#id")
    public void updateLastLogin(Long id, String ip) {
        log.info("更新最后登录信息, userId={}, ip={}", id, ip);

        try {
            User user = new User();
            user.setId(id);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateById(user);

            log.info("更新最后登录信息成功, userId={}", id);
        } catch (Exception e) {
            log.error("更新最后登录信息失败, userId={}", id, e);
            // 不抛出异常,避免影响登录流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("删除用户, id={}", id);

        // 检查用户是否存在
        User user = getById(id);

        // 逻辑删除
        userMapper.deleteById(id);

        // 清除缓存
        evictUserCache(id, user.getUsername(), user.getEmail());

        log.info("删除用户成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids) {
        log.info("批量删除用户, ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // 先获取用户信息用于清除缓存
        Map<Long, User> users = getByIds(ids);

        int count = userMapper.deleteByIds(ids);

        // 清除缓存
        users.values().forEach(user ->
            evictUserCache(user.getId(), user.getUsername(), user.getEmail())
        );

        log.info("批量删除用户成功, count={}", count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色, userId={}, roleIds={}", userId, roleIds);

        // 检查用户是否存在
        getById(userId);

        // 验证角色是否存在
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                Role role = roleMapper.selectById(roleId);
                if (role == null || role.getDeleted()) {
                    log.warn("角色不存在或已删除, roleId={}", roleId);
                    throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在: " + roleId);
                }
                // 检查角色状态
                if (role.getStatus() != null && role.getStatus() != 0) {
                    log.warn("角色已禁用, roleId={}, status={}", roleId, role.getStatus());
                    throw new BusinessException(ResultCode.PARAM_ERROR, "角色已禁用: " + role.getRoleName());
                }
            }
        }

        // 先删除现有角色
        userRoleMapper.deleteByUserId(userId);

        // 批量插入新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRoles.add(userRole);
            }
            userRoleMapper.batchInsert(userRoles);
        }

        // 清除用户角色和权限缓存
        evictCacheByKey("userRoles", userId);
        evictCacheByKey("userPermissions", userId);

        log.info("分配角色成功, userId={}, roleCount={}", userId, roleIds == null ? 0 : roleIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(Long userId, Long roleId) {
        log.info("为用户添加角色, userId={}, roleId={}", userId, roleId);

        // 检查用户是否存在
        getById(userId);

        // 验证角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted()) {
            log.warn("角色不存在或已删除, roleId={}", roleId);
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色不存在: " + roleId);
        }

        // 检查角色状态
        if (role.getStatus() != null && role.getStatus() != 0) {
            log.warn("角色已禁用, roleId={}, status={}", roleId, role.getStatus());
            throw new BusinessException(ResultCode.PARAM_ERROR, "角色已禁用: " + role.getRoleName());
        }

        // 检查是否已有该角色
        if (userRoleMapper.existsByUserIdAndRoleId(userId, roleId)) {
            log.warn("用户已拥有该角色, userId={}, roleId={}", userId, roleId);
            return;
        }

        // 添加角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        // 清除用户角色和权限缓存
        evictCacheByKey("userRoles", userId);
        evictCacheByKey("userPermissions", userId);

        log.info("添加角色成功, userId={}, roleId={}", userId, roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long userId, Long roleId) {
        log.info("移除用户角色, userId={}, roleId={}", userId, roleId);

        // 删除角色关联
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, userId)
                   .eq(UserRole::getRoleId, roleId);
        userRoleMapper.delete(queryWrapper);

        // 清除用户角色和权限缓存
        evictCacheByKey("userRoles", userId);
        evictCacheByKey("userPermissions", userId);

        log.info("移除角色成功, userId={}, roleId={}", userId, roleId);
    }

    @Override
    @Cacheable(value = "userRoles", key = "#userId")
    public List<Role> getUserRoles(Long userId) {
        log.info("查询用户角色, userId={}", userId);

        List<Role> roles = roleMapper.findByUserId(userId);

        log.info("查询用户角色成功, userId={}, roleCount={}", userId, roles.size());
        return roles;
    }

    @Override
    @Cacheable(value = "userPermissions", key = "#userId")
    public List<Permission> getUserPermissions(Long userId) {
        log.info("查询用户权限, userId={}", userId);

        List<Permission> permissions = permissionMapper.findByUserId(userId);

        log.info("查询用户权限成功, userId={}, permissionCount={}", userId, permissions.size());
        return permissions;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        log.debug("检查用户权限, userId={}, permissionCode={}", userId, permissionCode);

        if (StringUtils.isBlank(permissionCode)) {
            return false;
        }

        List<Permission> permissions = getUserPermissions(userId);
        return permissions.stream()
            .anyMatch(p -> permissionCode.equals(p.getPermissionCode()));
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        log.debug("检查用户角色, userId={}, roleCode={}", userId, roleCode);

        if (StringUtils.isBlank(roleCode)) {
            return false;
        }

        List<Role> roles = getUserRoles(userId);
        return roles.stream()
            .anyMatch(r -> roleCode.equals(r.getRoleCode()));
    }

    @Override
    public boolean verifyPassword(Long userId, String password) {
        log.debug("验证用户密码, userId={}", userId);

        if (StringUtils.isBlank(password)) {
            return false;
        }

        User user = getById(userId);
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public boolean existsByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }
        return userMapper.existsByUsername(username) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return userMapper.existsByEmail(email) > 0;
    }

    /**
     * 验证用户信息
     *
     * @param user 用户实体
     * @param isCreate 是否为创建操作
     */
    private void validateUser(User user, boolean isCreate) {
        if (user == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户信息不能为空");
        }

        if (isCreate) {
            if (StringUtils.isBlank(user.getUsername())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "用户名不能为空");
            }
            if (StringUtils.isBlank(user.getPassword())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能为空");
            }
        }

        // 验证用户名长度
        if (StringUtils.isNotBlank(user.getUsername()) &&
            (user.getUsername().length() < userProperties.getUsernameMinLength() || 
             user.getUsername().length() > userProperties.getUsernameMaxLength())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, 
                "用户名长度必须在" + userProperties.getUsernameMinLength() + 
                "-" + userProperties.getUsernameMaxLength() + "之间");
        }

        // 验证密码强度
        if (isCreate && StringUtils.isNotBlank(user.getPassword())) {
            passwordService.validatePasswordStrength(
                user.getPassword(),
                userProperties.getPasswordMinLength(),
                userProperties.getPasswordMaxLength()
            );
        }
    }
}