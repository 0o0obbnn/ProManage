package com.promanage.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.common.result.PageResult;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.domain.entity.Permission;
import com.promanage.domain.entity.Role;
import com.promanage.service.entity.RolePermission;
import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.RoleMapper;
import com.promanage.service.mapper.RolePermissionMapper;
import com.promanage.service.service.IRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 角色服务实现类
 *
 * <p>实现角色管理的所有业务逻辑,包括角色CRUD操作和权限分配。 使用Redis缓存提高查询性能,使用事务保证数据一致性。 支持角色权限管理和动态权限分配。
 *
 * <p>业务规则:
 *
 * <ul>
 *   <li>角色编码必须唯一,格式: ROLE_XXX
 *   <li>角色删除会逻辑删除,不会物理删除
 *   <li>角色权限分配是原子操作,先删除再添加
 *   <li>修改角色权限会清除相关用户缓存
 * </ul>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

  private final RoleMapper roleMapper;
  private final RolePermissionMapper rolePermissionMapper;
  private final PermissionMapper permissionMapper;

  /** 角色状态 - 正常 */
  private static final int ROLE_STATUS_NORMAL = 0;

  @Override
  public Map<Long, Role> getByIds(List<Long> ids) {
    log.info("批量查询角色, ids={}", ids);

    if (ids == null || ids.isEmpty()) {
      return Collections.emptyMap();
    }

    // 去重
    List<Long> uniqueIds =
        ids.stream().distinct().filter(id -> id != null).collect(Collectors.toList());

    if (uniqueIds.isEmpty()) {
      return Collections.emptyMap();
    }

    // 使用MyBatis-Plus的selectByIds方法批量查询
    List<Role> roles = roleMapper.selectBatchIds(uniqueIds);

    // 转换为Map，方便按ID查找
    Map<Long, Role> roleMap =
        roles.stream()
            .filter(role -> role != null && !role.getDeleted())
            .collect(Collectors.toMap(Role::getId, role -> role));

    log.info("批量查询角色完成, 请求数量={}, 查询到数量={}", uniqueIds.size(), roleMap.size());
    return roleMap;
  }

  @Override
  @Cacheable(value = "roles", key = "#id", unless = "#result == null")
  public Role getById(Long id) {
    log.info("查询角色详情, id={}", id);

    if (id == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID不能为空");
    }

    Role role = roleMapper.selectById(id);
    if (role == null || role.getDeleted()) {
      log.warn("角色不存在, id={}", id);
      throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
    }

    log.info("查询角色成功, id={}, roleName={}, roleCode={}", id, role.getRoleName(), role.getRoleCode());
    return role;
  }

  @Override
  @Cacheable(value = "roles", key = "'code:' + #roleCode", unless = "#result == null")
  public Role getByRoleCode(String roleCode) {
    log.info("根据角色编码查询角色, roleCode={}", roleCode);

    if (StringUtils.isBlank(roleCode)) {
      return null;
    }

    Role role = roleMapper.findByRoleCode(roleCode);
    if (role != null && !role.getDeleted()) {
      return role;
    }

    return null;
  }

  @Override
  public PageResult<Role> listRoles(Integer page, Integer pageSize, String keyword) {
    log.info("分页查询角色列表, page={}, pageSize={}, keyword={}", page, pageSize, keyword);

    // 构建分页对象
    Page<Role> pageParam = new Page<>(page, pageSize);

    // 构建查询条件
    LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Role::getDeleted, false);

    // 关键词搜索 (角色名称或角色编码)
    if (StringUtils.isNotBlank(keyword)) {
      queryWrapper.and(
          wrapper ->
              wrapper
                  .like(Role::getRoleName, keyword)
                  .or()
                  .like(Role::getRoleCode, keyword)
                  .or()
                  .like(Role::getDescription, keyword));
    }

    // 按排序字段和创建时间排序
    queryWrapper.orderByAsc(Role::getSort).orderByDesc(Role::getCreateTime);

    // 执行查询
    IPage<Role> pageResult = roleMapper.selectPage(pageParam, queryWrapper);

    log.info("查询角色列表成功, total={}, pages={}", pageResult.getTotal(), pageResult.getPages());

    return PageResult.of(pageResult.getRecords(), pageResult.getTotal(), page, pageSize);
  }

  @Override
  @Cacheable(value = "roles", key = "'all'")
  public List<Role> listAll() {
    log.info("查询所有角色");

    LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(Role::getDeleted, false)
        .eq(Role::getStatus, ROLE_STATUS_NORMAL)
        .orderByAsc(Role::getSort)
        .orderByDesc(Role::getCreateTime);

    List<Role> roles = roleMapper.selectList(queryWrapper);
    log.info("查询所有角色成功, count={}", roles.size());
    return roles;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(value = "roles", allEntries = true)
  public Long create(Role role) {
    log.info("创建角色, roleName={}, roleCode={}", role.getRoleName(), role.getRoleCode());

    // 参数验证
    if (role == null || StringUtils.isBlank(role.getRoleName())) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色信息不能为空");
    }

    if (StringUtils.isBlank(role.getRoleCode())) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色编码不能为空");
    }

    // 检查角色编码是否已存在
    if (existsByRoleCode(role.getRoleCode())) {
      log.warn("角色编码已存在, roleCode={}", role.getRoleCode());
      throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "角色编码已存在");
    }

    // 获取当前用户ID
    Long currentUserId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

    // 设置创建者和更新者
    role.setCreatorId(currentUserId);
    role.setUpdaterId(currentUserId);

    // 设置默认状态为正常
    if (role.getStatus() == null) {
      role.setStatus(ROLE_STATUS_NORMAL);
    }

    // 设置默认排序
    if (role.getSort() == null) {
      role.setSort(0);
    }

    // 插入角色
    int inserted = roleMapper.insert(role);
    if (inserted == 0) {
      log.error("创建角色失败, roleName={}, roleCode={}", role.getRoleName(), role.getRoleCode());
      throw new BusinessException(ResultCode.OPERATION_FAILED, "创建角色失败");
    }

    Long roleId = role.getId();
    log.info(
        "角色创建成功, roleId={}, roleName={}, roleCode={}",
        roleId,
        role.getRoleName(),
        role.getRoleCode());

    return roleId;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"roles", "users"},
      allEntries = true)
  public void update(Long id, Role role) {
    log.info("更新角色, id={}", id);

    // 参数验证
    if (id == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID不能为空");
    }

    if (role == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色信息不能为空");
    }

    // 检查角色是否存在
    Role existingRole = getById(id);

    // 如果修改了角色编码,检查新编码是否已存在
    if (StringUtils.isNotBlank(role.getRoleCode())
        && !role.getRoleCode().equals(existingRole.getRoleCode())) {
      if (existsByRoleCode(role.getRoleCode())) {
        log.warn("角色编码已存在, roleCode={}", role.getRoleCode());
        throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "角色编码已存在");
      }
    }

    // 获取当前用户ID
    Long currentUserId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

    // 设置更新信息
    role.setId(id);
    role.setUpdaterId(currentUserId);
    role.setUpdateTime(LocalDateTime.now());

    // 更新角色
    int updated = roleMapper.updateById(role);
    if (updated == 0) {
      log.error("更新角色失败, id={}", id);
      throw new BusinessException(ResultCode.OPERATION_FAILED, "更新角色失败");
    }

    log.info("角色更新成功, id={}", id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"roles", "users"},
      allEntries = true)
  public void delete(Long id) {
    log.info("删除角色, id={}", id);

    // 参数验证
    if (id == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID不能为空");
    }

    // 检查角色是否存在
    Role role = getById(id);

    // 获取当前用户ID
    Long currentUserId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

    // 逻辑删除角色
    role.setDeleted(true);
    role.setUpdaterId(currentUserId);
    role.setUpdateTime(LocalDateTime.now());

    int deleted = roleMapper.updateById(role);
    if (deleted == 0) {
      log.error("删除角色失败, id={}", id);
      throw new BusinessException(ResultCode.OPERATION_FAILED, "删除角色失败");
    }

    // 删除角色关联的权限
    try {
      rolePermissionMapper.deleteByRoleId(id);
      log.info("删除角色关联的权限成功, roleId={}", id);
    } catch (DataAccessException e) {
      log.error("删除角色关联的权限失败, roleId={}", id, e);
      // 不抛出异常,避免影响角色删除
    }

    log.info("角色删除成功, id={}", id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"roles", "users"},
      allEntries = true)
  public int batchDelete(List<Long> ids) {
    log.info("批量删除角色, ids={}", ids);

    // 参数验证
    if (ids == null || ids.isEmpty()) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID列表不能为空");
    }

    // 获取当前用户ID
    Long currentUserId =
        SecurityUtils.getCurrentUserId()
            .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

    int deleteCount = 0;
    for (Long id : ids) {
      try {
        // 检查角色是否存在
        Role role = roleMapper.selectById(id);
        if (role != null && !role.getDeleted()) {
          // 逻辑删除
          role.setDeleted(true);
          role.setUpdaterId(currentUserId);
          role.setUpdateTime(LocalDateTime.now());

          int deleted = roleMapper.updateById(role);
          if (deleted > 0) {
            deleteCount++;

            // 删除角色关联的权限
            try {
              rolePermissionMapper.deleteByRoleId(id);
            } catch (DataAccessException e) {
              log.error("删除角色关联的权限失败, roleId={}", id, e);
            }
          }
        }
      } catch (DataAccessException e) {
        log.error("删除角色失败, id={}", id, e);
        // 继续处理下一个
      }
    }

    log.info("批量删除角色完成, 成功删除{}个角色", deleteCount);
    return deleteCount;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @CacheEvict(
      value = {"roles", "users", "rolePermissions"},
      allEntries = true)
  public void assignPermissions(Long roleId, List<Long> permissionIds) {
    log.info("为角色分配权限, roleId={}, permissionIds={}", roleId, permissionIds);

    // 参数验证
    if (roleId == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID不能为空");
    }

    // 检查角色是否存在
    getById(roleId);

    // 1. 删除角色现有的所有权限关联
    try {
      int deletedCount = rolePermissionMapper.deleteByRoleId(roleId);
      log.info("删除角色现有权限关联, roleId={}, deletedCount={}", roleId, deletedCount);
    } catch (DataAccessException e) {
      log.error("删除角色现有权限关联失败, roleId={}", roleId, e);
      throw new BusinessException(ResultCode.OPERATION_FAILED, "删除角色现有权限关联失败");
    }

    // 2. 如果权限ID列表为空或null,则只删除不添加
    if (permissionIds == null || permissionIds.isEmpty()) {
      log.info("权限ID列表为空,仅删除现有权限, roleId={}", roleId);
      return;
    }

    // 3. 批量插入新的权限关联
    List<RolePermission> rolePermissions = new ArrayList<>();
    for (Long permissionId : permissionIds) {
      // 验证权限是否存在
      Permission permission = permissionMapper.selectById(permissionId);
      if (permission == null || permission.getDeleted()) {
        log.warn("权限不存在或已删除,跳过, permissionId={}", permissionId);
        continue;
      }

      RolePermission rolePermission = new RolePermission();
      rolePermission.setRoleId(roleId);
      rolePermission.setPermissionId(permissionId);
      // createdBy和updatedBy由MyBatisMetaObjectHandler自动填充

      rolePermissions.add(rolePermission);
    }

    // 批量插入
    if (!rolePermissions.isEmpty()) {
      try {
        int insertedCount = rolePermissionMapper.batchInsert(rolePermissions);
        log.info("批量插入角色权限关联成功, roleId={}, insertedCount={}", roleId, insertedCount);
      } catch (DataAccessException e) {
        log.error("批量插入角色权限关联失败, roleId={}", roleId, e);
        throw new BusinessException(ResultCode.OPERATION_FAILED, "分配权限失败");
      }
    }

    log.info("为角色分配权限成功, roleId={}, assignedCount={}", roleId, rolePermissions.size());
  }

  @Override
  @Cacheable(value = "rolePermissions", key = "'role:' + #roleId")
  public List<Permission> getRolePermissions(Long roleId) {
    log.info("查询角色的所有权限, roleId={}", roleId);

    // 参数验证
    if (roleId == null) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "角色ID不能为空");
    }

    // 检查角色是否存在
    getById(roleId);

    List<Permission> permissions = permissionMapper.findByRoleId(roleId);

    // 过滤掉已删除或禁用的权限
    List<Permission> activePermissions =
        permissions.stream()
            .filter(p -> !p.getDeleted() && p.getStatus() == 0)
            .collect(Collectors.toList());

    log.info("查询角色权限成功, roleId={}, permissionCount={}", roleId, activePermissions.size());
    return activePermissions;
  }

  @Override
  public boolean existsByRoleCode(String roleCode) {
    log.debug("检查角色编码是否存在, roleCode={}", roleCode);

    if (StringUtils.isBlank(roleCode)) {
      return false;
    }

    boolean exists = roleMapper.existsByRoleCode(roleCode);
    log.debug("检查结果: roleCode={}, exists={}", roleCode, exists);
    return exists;
  }
}
