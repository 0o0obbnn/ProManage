package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.service.entity.Permission;
import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.RolePermissionMapper;
import com.promanage.service.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * <p>
 * 实现权限管理的所有业务逻辑,包括权限CRUD操作和树形结构查询。
 * 使用Redis缓存提高查询性能,使用事务保证数据一致性。
 * 支持菜单权限、按钮权限和API权限的统一管理。
 * </p>
 *
 * <p>
 * 业务规则:
 * <ul>
 *   <li>权限编码必须唯一,格式: 模块:操作 (如: document:create)</li>
 *   <li>权限删除会逻辑删除,不会物理删除</li>
 *   <li>支持树形权限结构,通过parentId构建层级关系</li>
 *   <li>删除父权限不会级联删除子权限</li>
 * </ul>
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 权限状态 - 正常
     */
    private static final int PERMISSION_STATUS_NORMAL = 0;


    /**
     * 顶级权限的父ID
     */
    private static final Long TOP_PARENT_ID = 0L;

    @Override
    @Cacheable(value = "permissions", key = "#id", unless = "#result == null")
    public Permission getById(Long id) {
        log.info("查询权限详情, id={}", id);

        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限ID不能为空");
        }

        Permission permission = permissionMapper.selectById(id);
        if (permission == null || permission.getDeleted()) {
            log.warn("权限不存在, id={}", id);
            throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND);
        }

        log.info("查询权限成功, id={}, permissionName={}, permissionCode={}",
                id, permission.getPermissionName(), permission.getPermissionCode());
        return permission;
    }

    @Override
    @Cacheable(value = "permissions", key = "'code:' + #permissionCode", unless = "#result == null")
    public Permission getByPermissionCode(String permissionCode) {
        log.info("根据权限编码查询权限, permissionCode={}", permissionCode);

        if (StringUtils.isBlank(permissionCode)) {
            return null;
        }

        Permission permission = permissionMapper.findByPermissionCode(permissionCode);
        if (permission != null && !permission.getDeleted()) {
            return permission;
        }

        return null;
    }

    @Override
    public PageResult<Permission> listPermissions(Integer page, Integer pageSize, String keyword) {
        log.info("分页查询权限列表, page={}, pageSize={}, keyword={}", page, pageSize, keyword);

        // 构建分页对象
        Page<Permission> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, false);

        // 关键词搜索 (权限名称或权限编码)
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Permission::getPermissionName, keyword)
                    .or()
                    .like(Permission::getPermissionCode, keyword)
            );
        }

        // 按排序字段和创建时间排序
        queryWrapper.orderByAsc(Permission::getSort)
                .orderByDesc(Permission::getCreateTime);

        // 执行查询
        IPage<Permission> pageResult = permissionMapper.selectPage(pageParam, queryWrapper);

        log.info("查询权限列表成功, total={}, pages={}", pageResult.getTotal(), pageResult.getPages());

        return PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                page,
                pageSize
        );
    }

    @Override
    @Cacheable(value = "permissions", key = "'all'")
    public List<Permission> listAll() {
        log.info("查询所有权限");

        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getDeleted, false)
                .eq(Permission::getStatus, PERMISSION_STATUS_NORMAL)
                .orderByAsc(Permission::getSort)
                .orderByDesc(Permission::getCreateTime);

        List<Permission> permissions = permissionMapper.selectList(queryWrapper);
        log.info("查询所有权限成功, count={}", permissions.size());
        return permissions;
    }

    @Override
    @Cacheable(value = "permissions", key = "'tree'")
    public List<Permission> listTree() {
        log.info("查询权限树");

        // 查询所有权限
        List<Permission> allPermissions = listAll();

        // 构建树形结构
        List<Permission> tree = buildTree(allPermissions, TOP_PARENT_ID);

        log.info("构建权限树成功, rootNodeCount={}", tree.size());
        return tree;
    }

    @Override
    public List<Permission> listByParentId(Long parentId) {
        log.info("根据父级ID查询子权限, parentId={}", parentId);

        if (parentId == null) {
            parentId = TOP_PARENT_ID;
        }

        List<Permission> permissions = permissionMapper.findByParentId(parentId);

        // 过滤掉已删除或禁用的权限
        List<Permission> activePermissions = permissions.stream()
                .filter(p -> !p.getDeleted() && p.getStatus() == PERMISSION_STATUS_NORMAL)
                .sorted((p1, p2) -> {
                    // 先按sort排序,再按创建时间排序
                    int sortCompare = Integer.compare(
                            p1.getSort() != null ? p1.getSort() : 0,
                            p2.getSort() != null ? p2.getSort() : 0
                    );
                    if (sortCompare != 0) {
                        return sortCompare;
                    }
                    return p2.getCreateTime().compareTo(p1.getCreateTime());
                })
                .collect(Collectors.toList());

        log.info("查询子权限成功, parentId={}, count={}", parentId, activePermissions.size());
        return activePermissions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"permissions", "roles", "users"}, allEntries = true)
    public Long create(Permission permission) {
        log.info("创建权限, permissionName={}, permissionCode={}",
                permission.getPermissionName(), permission.getPermissionCode());

        // 参数验证
        if (permission == null || StringUtils.isBlank(permission.getPermissionName())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限信息不能为空");
        }

        if (StringUtils.isBlank(permission.getPermissionCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限编码不能为空");
        }

        // 检查权限编码是否已存在
        if (existsByPermissionCode(permission.getPermissionCode())) {
            log.warn("权限编码已存在, permissionCode={}", permission.getPermissionCode());
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "权限编码已存在");
        }

        // 如果设置了父级ID,验证父级权限是否存在
        if (permission.getParentId() != null && permission.getParentId() > 0) {
            Permission parentPermission = permissionMapper.selectById(permission.getParentId());
            if (parentPermission == null || parentPermission.getDeleted()) {
                log.warn("父级权限不存在, parentId={}", permission.getParentId());
                throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "父级权限不存在");
            }
        } else {
            // 设置为顶级权限
            permission.setParentId(TOP_PARENT_ID);
        }

        // 获取当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

        // 设置创建者和更新者
        permission.setCreatorId(currentUserId);
        permission.setUpdaterId(currentUserId);

        // 设置默认状态为正常
        if (permission.getStatus() == null) {
            permission.setStatus(PERMISSION_STATUS_NORMAL);
        }

        // 设置默认排序
        if (permission.getSort() == null) {
            permission.setSort(0);
        }

        // 插入权限
        int inserted = permissionMapper.insert(permission);
        if (inserted == 0) {
            log.error("创建权限失败, permissionName={}, permissionCode={}",
                    permission.getPermissionName(), permission.getPermissionCode());
            throw new BusinessException(ResultCode.OPERATION_FAILED, "创建权限失败");
        }

        Long permissionId = permission.getId();
        log.info("权限创建成功, permissionId={}, permissionName={}, permissionCode={}",
                permissionId, permission.getPermissionName(), permission.getPermissionCode());

        return permissionId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"permissions", "roles", "users"}, allEntries = true)
    public void update(Long id, Permission permission) {
        log.info("更新权限, id={}", id);

        // 参数验证
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限ID不能为空");
        }

        if (permission == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限信息不能为空");
        }

        // 检查权限是否存在
        Permission existingPermission = getById(id);

        // 如果修改了权限编码,检查新编码是否已存在
        if (StringUtils.isNotBlank(permission.getPermissionCode())
                && !permission.getPermissionCode().equals(existingPermission.getPermissionCode())) {
            if (existsByPermissionCode(permission.getPermissionCode())) {
                log.warn("权限编码已存在, permissionCode={}", permission.getPermissionCode());
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS, "权限编码已存在");
            }
        }

        // 如果修改了父级ID,验证父级权限是否存在,且不能将自己设置为父级
        if (permission.getParentId() != null) {
            if (permission.getParentId().equals(id)) {
                log.warn("不能将权限的父级设置为自己, id={}, parentId={}", id, permission.getParentId());
                throw new BusinessException(ResultCode.PARAM_ERROR, "不能将权限的父级设置为自己");
            }

            if (permission.getParentId() > 0) {
                Permission parentPermission = permissionMapper.selectById(permission.getParentId());
                if (parentPermission == null || parentPermission.getDeleted()) {
                    log.warn("父级权限不存在, parentId={}", permission.getParentId());
                    throw new BusinessException(ResultCode.PERMISSION_NOT_FOUND, "父级权限不存在");
                }

                // 检查是否会造成循环引用
                if (isCircularReference(id, permission.getParentId())) {
                    log.warn("不能设置父级权限,会造成循环引用, id={}, parentId={}", id, permission.getParentId());
                    throw new BusinessException(ResultCode.PARAM_ERROR, "不能设置父级权限,会造成循环引用");
                }
            }
        }

        // 获取当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

        // 设置更新信息
        permission.setId(id);
        permission.setUpdaterId(currentUserId);
        permission.setUpdateTime(LocalDateTime.now());

        // 更新权限
        int updated = permissionMapper.updateById(permission);
        if (updated == 0) {
            log.error("更新权限失败, id={}", id);
            throw new BusinessException(ResultCode.OPERATION_FAILED, "更新权限失败");
        }

        log.info("权限更新成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"permissions", "roles", "users"}, allEntries = true)
    public void delete(Long id) {
        log.info("删除权限, id={}", id);

        // 参数验证
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限ID不能为空");
        }

        // 检查权限是否存在
        Permission permission = getById(id);

        // 检查是否有子权限
        List<Permission> children = permissionMapper.findByParentId(id);
        long activeChildrenCount = children.stream()
                .filter(p -> !p.getDeleted())
                .count();

        if (activeChildrenCount > 0) {
            log.warn("权限下有子权限,不能删除, id={}, childrenCount={}", id, activeChildrenCount);
            throw new BusinessException(ResultCode.OPERATION_FAILED,
                    "权限下有子权限,不能删除。请先删除或移动子权限。");
        }

        // 获取当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

        // 逻辑删除权限
        permission.setDeleted(true);
        permission.setUpdaterId(currentUserId);
        permission.setUpdateTime(LocalDateTime.now());

        int deleted = permissionMapper.updateById(permission);
        if (deleted == 0) {
            log.error("删除权限失败, id={}", id);
            throw new BusinessException(ResultCode.OPERATION_FAILED, "删除权限失败");
        }

        // 删除权限关联的角色关系
        try {
            rolePermissionMapper.deleteByPermissionId(id);
            log.info("删除权限关联的角色关系成功, permissionId={}", id);
        } catch (Exception e) {
            log.error("删除权限关联的角色关系失败, permissionId={}", id, e);
            // 不抛出异常,避免影响权限删除
        }

        log.info("权限删除成功, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"permissions", "roles", "users"}, allEntries = true)
    public int batchDelete(List<Long> ids) {
        log.info("批量删除权限, ids={}", ids);

        // 参数验证
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "权限ID列表不能为空");
        }

        // 获取当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "未登录"));

        int deleteCount = 0;
        for (Long id : ids) {
            try {
                // 检查权限是否存在
                Permission permission = permissionMapper.selectById(id);
                if (permission != null && !permission.getDeleted()) {
                    // 检查是否有子权限
                    List<Permission> children = permissionMapper.findByParentId(id);
                    long activeChildrenCount = children.stream()
                            .filter(p -> !p.getDeleted())
                            .count();

                    if (activeChildrenCount > 0) {
                        log.warn("权限下有子权限,跳过删除, id={}, childrenCount={}", id, activeChildrenCount);
                        continue;
                    }

                    // 逻辑删除
                    permission.setDeleted(true);
                    permission.setUpdaterId(currentUserId);
                    permission.setUpdateTime(LocalDateTime.now());

                    int deleted = permissionMapper.updateById(permission);
                    if (deleted > 0) {
                        deleteCount++;

                        // 删除权限关联的角色关系
                        try {
                            rolePermissionMapper.deleteByPermissionId(id);
                        } catch (Exception e) {
                            log.error("删除权限关联的角色关系失败, permissionId={}", id, e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("删除权限失败, id={}", id, e);
                // 继续处理下一个
            }
        }

        log.info("批量删除权限完成, 成功删除{}个权限", deleteCount);
        return deleteCount;
    }

    @Override
    public boolean existsByPermissionCode(String permissionCode) {
        log.debug("检查权限编码是否存在, permissionCode={}", permissionCode);

        if (StringUtils.isBlank(permissionCode)) {
            return false;
        }

        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Permission::getPermissionCode, permissionCode)
                .eq(Permission::getDeleted, false);

        long count = permissionMapper.selectCount(queryWrapper);
        boolean exists = count > 0;

        log.debug("检查结果: permissionCode={}, exists={}", permissionCode, exists);
        return exists;
    }

    /**
     * 构建权限树
     * <p>
     * 递归构建权限的树形结构
     * </p>
     *
     * @param allPermissions 所有权限列表
     * @param parentId 父级ID
     * @return 权限树列表
     */
    private List<Permission> buildTree(List<Permission> allPermissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();

        if (allPermissions == null || allPermissions.isEmpty()) {
            return tree;
        }

        // 使用Map提高查询效率
        Map<Long, List<Permission>> parentChildrenMap = new HashMap<>();
        for (Permission permission : allPermissions) {
            Long pid = permission.getParentId() != null ? permission.getParentId() : TOP_PARENT_ID;
            parentChildrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(permission);
        }

        // 构建树形结构
        return buildTreeRecursive(parentChildrenMap, parentId);
    }

    /**
     * 递归构建权限树
     *
     * @param parentChildrenMap 父子关系映射
     * @param parentId 父级ID
     * @return 权限树列表
     */
    private List<Permission> buildTreeRecursive(Map<Long, List<Permission>> parentChildrenMap, Long parentId) {
        List<Permission> children = parentChildrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        // 排序
        children.sort((p1, p2) -> {
            int sortCompare = Integer.compare(
                    p1.getSort() != null ? p1.getSort() : 0,
                    p2.getSort() != null ? p2.getSort() : 0
            );
            if (sortCompare != 0) {
                return sortCompare;
            }
            return p2.getCreateTime().compareTo(p1.getCreateTime());
        });

        // 递归构建子树
        for (Permission permission : children) {
            List<Permission> subChildren = buildTreeRecursive(parentChildrenMap, permission.getId());
            // 设置子权限列表
            if (!subChildren.isEmpty()) {
                permission.setChildren(subChildren);
            }
        }

        return children;
    }

    /**
     * 检查是否会造成循环引用
     * <p>
     * 检查将指定权限的父级设置为targetParentId是否会造成循环引用
     * </p>
     *
     * @param permissionId 权限ID
     * @param targetParentId 目标父级ID
     * @return true表示会造成循环引用
     */
    private boolean isCircularReference(Long permissionId, Long targetParentId) {
        if (targetParentId == null || targetParentId <= 0) {
            return false;
        }

        // 向上查找,检查targetParentId的祖先节点中是否包含permissionId
        Long currentId = targetParentId;
        int maxDepth = 100; // 防止无限循环
        int depth = 0;

        while (currentId != null && currentId > 0 && depth < maxDepth) {
            if (currentId.equals(permissionId)) {
                return true;
            }

            Permission parent = permissionMapper.selectById(currentId);
            if (parent == null || parent.getDeleted()) {
                break;
            }

            currentId = parent.getParentId();
            depth++;
        }

        return false;
    }
}
