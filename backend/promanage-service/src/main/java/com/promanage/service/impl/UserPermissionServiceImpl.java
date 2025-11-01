package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
import com.promanage.domain.entity.UserRole;
import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.RolePermissionMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.UserRoleMapper;
import com.promanage.service.service.IRoleService;
import com.promanage.service.service.IUserPermissionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户权限服务实现类
 *
 * <p>提供用户权限查询和验证相关功能
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionServiceImpl implements IUserPermissionService {

  private final UserMapper userMapper;

  private final UserRoleMapper userRoleMapper;

  private final IRoleService roleService;
  private final RolePermissionMapper rolePermissionMapper;
  private final PermissionMapper permissionMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"users", "roles"},
      allEntries = true)
  public void assignRolesToUser(Long userId, List<Long> roleIds) {
    log.info("为用户分配角色, userId={}, roleIds={}", userId, roleIds);

    // 验证用户存在
    User user = userMapper.selectById(userId);
    if (user == null || user.getDeleted()) {
      throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
    }

    // 验证角色ID列表
    if (CollectionUtils.isEmpty(roleIds)) {
      throw new BusinessException("角色ID列表不能为空");
    }

    // 验证所有角色ID都存在
    for (Long roleId : roleIds) {
      Role role = roleService.getById(roleId);
      if (role == null || role.getDeleted()) {
        throw new BusinessException("角色不存在或已删除, roleId=" + roleId);
      }
    }

    // 删除用户原有的角色关联
    userRoleMapper.deleteByUserId(userId);

    // 批量插入新的角色关联
    List<UserRole> userRoles =
        roleIds.stream()
            .map(
                roleId -> {
                  UserRole userRole = new UserRole();
                  userRole.setUserId(userId);
                  userRole.setRoleId(roleId);
                  userRole.setCreateTime(LocalDateTime.now());
                  return userRole;
                })
            .collect(Collectors.toList());

    userRoleMapper.insertBatch(userRoles);

    log.info("用户角色分配成功, userId={}", userId);
  }

  @Override
  public Set<Long> getUserRoleIds(Long userId) {
    if (userId == null) {
      return Set.of();
    }

    List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
    return new HashSet<>(roleIds);
  }

  @Override
  @Cacheable(value = "users", key = "'user_role_codes_' + #userId")
  public List<String> getUserRoleCodes(Long userId) {
    log.debug("获取用户拥有的角色编码列表, userId={}", userId);

    return userRoleMapper.selectRoleCodesByUserId(userId);
  }

  @Override
  @Cacheable(value = "users", key = "'user_with_roles_' + #userId")
  public User getUserWithRoles(Long userId) {
    log.debug("获取用户信息及其角色, userId={}", userId);

    User user = userMapper.selectById(userId);
    if (user == null || user.getDeleted()) {
      throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
    }

    // Note: User实体类中没有setRoleIds方法，所以无法将角色ID列表设置到User对象中
    // 如果需要角色信息，可以通过getUserRoleIds()或getUserRoles()方法单独获取

    return user;
  }

  @Override
  public List<UserRole> getUserRoles(Long userId) {
    log.debug("获取用户角色列表, userId={}", userId);

    return userRoleMapper.selectByUserId(userId);
  }

  @Override
  @Cacheable(value = "users", key = "'user_permissions_' + #userId")
  public List<PermissionResponse> getUserPermissions(Long userId) {
    log.debug("获取用户最终有效权限列表, userId={}", userId);

    User user = userMapper.selectById(userId);
    if (user == null || user.getDeleted()) {
      throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
    }

    List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
    if (CollectionUtils.isEmpty(roleIds)) {
      return List.of();
    }

    Set<Long> permissionIds = new HashSet<>();
    for (Long roleId : roleIds) {
      List<Long> rolePermIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
      if (rolePermIds != null) {
        permissionIds.addAll(rolePermIds);
      }
    }

    if (permissionIds.isEmpty()) {
      return List.of();
    }

    List<Permission> permissions = permissionMapper.selectBatchIds(permissionIds);
    return permissions.stream()
        .map(
            p -> {
              PermissionResponse resp = new PermissionResponse();
              BeanUtils.copyProperties(p, resp);
              return resp;
            })
        .collect(Collectors.toList());
  }
}
