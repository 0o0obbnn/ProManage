package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.promanage.common.entity.Organization;
import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.dto.request.CreatePermissionRequest;
import com.promanage.service.dto.request.UpdatePermissionRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.service.dto.response.PermissionTreeResponse;
import com.promanage.service.entity.ChangeRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.entity.Notification;
import com.promanage.service.entity.Permission;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.entity.Role;
import com.promanage.service.entity.RolePermission;
import com.promanage.service.entity.Task;
import com.promanage.service.entity.UserRole;
import com.promanage.service.mapper.ChangeRequestMapper;
import com.promanage.service.mapper.DocumentMapper;
import com.promanage.service.mapper.NotificationMapper;
import com.promanage.service.mapper.OrganizationMapper;
import com.promanage.service.mapper.PermissionMapper;
import com.promanage.service.mapper.ProjectMapper;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.mapper.RoleMapper;
import com.promanage.service.mapper.TaskMapper;
import com.promanage.service.mapper.UserMapper;
import com.promanage.service.mapper.RolePermissionMapper;
import com.promanage.service.mapper.UserRoleMapper;
import com.promanage.service.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 * <p>
 * 提供权限管理的核心业务逻辑，包括：
 * - 权限的CRUD操作
 * - 权限树形结构构建
 * - 角色权限分配
 * - 用户权限查询和验证
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    private static final Set<String> SUPER_ADMIN_ROLE_CODES = Set.of("ROLE_SUPER_ADMIN", "SUPER_ADMIN", "SYSTEM_ADMIN");
    private static final Set<Long> PROJECT_ADMIN_ROLE_IDS = Set.of(1L);

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final TaskMapper taskMapper;
    private final DocumentMapper documentMapper;
    private final NotificationMapper notificationMapper;
    private final ChangeRequestMapper changeRequestMapper;
    private final RoleMapper roleMapper;

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", allEntries = true)
    public Long createPermission(CreatePermissionRequest request) {
        log.info("创建权限, permissionName={}, permissionCode={}", request.getPermissionName(), request.getPermissionCode());

        // 验证权限编码唯一性
        validatePermissionCodeUnique(request.getPermissionCode(), null);

        // 验证父级权限存在性
        if (request.getParentId() != null && request.getParentId() > 0) {
            validatePermissionExists(request.getParentId());
        }

        // 创建权限实体
        Permission permission = new Permission();
        BeanUtils.copyProperties(request, permission);
        
        // 设置默认值
        if (permission.getStatus() == null) {
            permission.setStatus(0); // 默认状态：正常
        }
        if (permission.getSort() == null) {
            permission.setSort(0); // 默认排序
        }
        if (permission.getParentId() == null) {
            permission.setParentId(0L); // 默认为顶级权限
        }

        permission.setCreateTime(LocalDateTime.now());
        permission.setDeleted(false);

        permissionMapper.insert(permission);

        log.info("权限创建成功, permissionId={}, permissionCode={}", permission.getId(), permission.getPermissionCode());
        return permission.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", allEntries = true)
    public Boolean updatePermission(Long id, UpdatePermissionRequest request) {
        log.info("更新权限, permissionId={}", id);

        // 验证权限存在
        Permission existingPermission = validatePermissionExists(id);

        // 验证权限编码唯一性（如果修改了编码）
        if (StringUtils.hasText(request.getPermissionCode()) && 
            !request.getPermissionCode().equals(existingPermission.getPermissionCode())) {
            validatePermissionCodeUnique(request.getPermissionCode(), id);
        }

        // 验证父级权限（不能将权限设置为自己的子权限）
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("权限不能设置为自己的子权限");
            }
            validatePermissionExists(request.getParentId());
            
            // 检查是否会形成循环引用
            if (wouldCreateCircularReference(id, request.getParentId())) {
                throw new BusinessException("不能设置此父级权限，会形成循环引用");
            }
        }

        // 更新权限信息
        Permission permission = new Permission();
        permission.setId(id);
        BeanUtils.copyProperties(request, permission);
        permission.setUpdateTime(LocalDateTime.now());

        int updated = permissionMapper.updateById(permission);

        log.info("权限更新成功, permissionId={}", id);
        return updated > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "permissions", allEntries = true)
    public Boolean deletePermission(Long id) {
        log.info("删除权限, permissionId={}", id);

        // 验证权限存在
        validatePermissionExists(id);

        // 检查是否有子权限
        List<Permission> children = permissionMapper.findByParentId(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BusinessException("存在子权限，无法删除");
        }

        // 检查是否有角色正在使用此权限
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getPermissionId, id);
        Long count = rolePermissionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("权限正在被角色使用，无法删除");
        }

        // 软删除
        Permission permission = new Permission();
        permission.setId(id);
        permission.setDeleted(true);
        permission.setUpdateTime(LocalDateTime.now());
        
        int deleted = permissionMapper.updateById(permission);

        log.info("权限删除成功, permissionId={}", id);
        return deleted > 0;
    }

    @Cacheable(value = "permissions", key = "#id")
    public PermissionResponse getPermissionById(Long id) {
        log.debug("根据ID获取权限, permissionId={}", id);

        Permission permission = validatePermissionExists(id);
        
        return convertToResponse(permission);
    }

    @Cacheable(value = "permissions", key = "'all'")
    public List<PermissionResponse> listAllPermissions() {
        log.debug("获取所有权限列表");

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, false)
                .orderByAsc(Permission::getSort)
                .orderByAsc(Permission::getCreateTime);

        List<Permission> permissions = permissionMapper.selectList(wrapper);

        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "permissions", key = "'type:' + #type")
    public List<PermissionResponse> listPermissionsByType(String type) {
        log.debug("根据类型获取权限列表, type={}", type);

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, type)
                .eq(Permission::getDeleted, false)
                .orderByAsc(Permission::getSort)
                .orderByAsc(Permission::getCreateTime);

        List<Permission> permissions = permissionMapper.selectList(wrapper);

        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "permissions", key = "'tree'")
    public List<PermissionTreeResponse> getPermissionTree() {
        log.debug("获取权限树形结构");

        // 查询所有权限
        List<Permission> allPermissions = listAllPermissionsInternal();

        // 构建权限树
        return buildPermissionTree(allPermissions, 0L);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"permissions", "rolePermissions"}, allEntries = true)
    public Boolean assignPermissionsToRole(AssignPermissionsRequest request) {
        log.info("为角色分配权限, roleId={}, permissionIds={}", request.getRoleId(), request.getPermissionIds());

        Long roleId = request.getRoleId();
        List<Long> permissionIds = request.getPermissionIds();

        // 验证权限ID列表
        if (CollectionUtils.isEmpty(permissionIds)) {
            // 清空角色的所有权限
            LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<RolePermission>()
                    .eq(RolePermission::getRoleId, roleId);
            rolePermissionMapper.delete(wrapper);
            
            log.info("清空角色权限成功, roleId={}", roleId);
            return true;
        }

        // 验证所有权限是否存在
        for (Long permissionId : permissionIds) {
            validatePermissionExists(permissionId);
        }

        // 删除角色原有的所有权限关联
        LambdaQueryWrapper<RolePermission> deleteWrapper = new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(deleteWrapper);

        // 批量插入新的权限关联
        List<RolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(roleId);
                    rolePermission.setPermissionId(permissionId);
                    rolePermission.setCreateTime(LocalDateTime.now());
                    return rolePermission;
                })
                .collect(Collectors.toList());

        // 使用 MyBatis-Plus 的批量插入
        for (RolePermission rolePermission : rolePermissions) {
            rolePermissionMapper.insert(rolePermission);
        }

        log.info("角色权限分配成功, roleId={}, 权限数量={}", roleId, permissionIds.size());
        return true;
    }

    @Cacheable(value = "rolePermissions", key = "#roleId")
    public List<PermissionResponse> getRolePermissions(Long roleId) {
        log.debug("获取角色权限列表, roleId={}", roleId);

        // 查询角色关联的权限ID列表
        LambdaQueryWrapper<RolePermission> wrapper = new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(rolePermissions)) {
            return new ArrayList<>();
        }

        // 提取权限ID列表
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 批量查询权限详情
        LambdaQueryWrapper<Permission> permissionWrapper = new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, permissionIds)
                .eq(Permission::getDeleted, false)
                .eq(Permission::getStatus, 0); // 只返回正常状态的权限

        List<Permission> permissions = permissionMapper.selectList(permissionWrapper);

        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "userPermissions", key = "#userId")
    public List<PermissionResponse> getUserPermissions(Long userId) {
        log.debug("获取用户权限列表, userId={}", userId);

        // 查询用户的所有角色
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);

        if (CollectionUtils.isEmpty(userRoles)) {
            return new ArrayList<>();
        }

        // 提取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        // 查询所有角色关联的权限（去重）
        LambdaQueryWrapper<RolePermission> rolePermissionWrapper = new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getRoleId, roleIds);
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(rolePermissionWrapper);

        if (CollectionUtils.isEmpty(rolePermissions)) {
            return new ArrayList<>();
        }

        // 提取权限ID列表（去重）
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询权限详情
        LambdaQueryWrapper<Permission> permissionWrapper = new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, permissionIds)
                .eq(Permission::getDeleted, false)
                .eq(Permission::getStatus, 0); // 只返回正常状态的权限

        List<Permission> permissions = permissionMapper.selectList(permissionWrapper);

        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "userPermissionCheck", key = "#userId + ':' + #permissionCode")
    public Boolean checkUserPermission(Long userId, String permissionCode) {
        log.debug("检查用户权限, userId={}, permissionCode={}", userId, permissionCode);

        // 获取用户的所有权限
        List<PermissionResponse> userPermissions = getUserPermissions(userId);

        // 检查是否包含指定权限编码
        return userPermissions.stream()
                .anyMatch(permission -> permission.getPermissionCode().equals(permissionCode));
    }

    public boolean hasPermissionViewPermission(Long userId) {
        return checkUserPermission(userId, "permission:view");
    }

    public boolean hasPermissionCreatePermission(Long userId) {
        return checkUserPermission(userId, "permission:create");
    }

    public boolean hasPermissionEditPermission(Long userId) {
        return checkUserPermission(userId, "permission:edit");
    }

    public boolean hasPermissionDeletePermission(Long userId) {
        return checkUserPermission(userId, "permission:delete");
    }

    public boolean hasPermissionAssignPermission(Long userId) {
        return checkUserPermission(userId, "permission:assign");
    }

    public Object getPermissionStatistics() {
        log.debug("获取权限统计信息");

        Map<String, Object> statistics = new HashMap<>();

        // 统计总权限数
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, false);
        Long totalCount = permissionMapper.selectCount(wrapper);
        statistics.put("totalCount", totalCount);

        // 统计各类型权限数量
        Map<String, Long> typeCount = new HashMap<>();
        
        LambdaQueryWrapper<Permission> menuWrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, "menu")
                .eq(Permission::getDeleted, false);
        typeCount.put("menu", permissionMapper.selectCount(menuWrapper));
        
        LambdaQueryWrapper<Permission> buttonWrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, "button")
                .eq(Permission::getDeleted, false);
        typeCount.put("button", permissionMapper.selectCount(buttonWrapper));
        
        LambdaQueryWrapper<Permission> apiWrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, "api")
                .eq(Permission::getDeleted, false);
        typeCount.put("api", permissionMapper.selectCount(apiWrapper));
        
        statistics.put("typeCount", typeCount);

        // 统计启用/禁用状态
        LambdaQueryWrapper<Permission> activeWrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getStatus, 0)
                .eq(Permission::getDeleted, false);
        statistics.put("activeCount", permissionMapper.selectCount(activeWrapper));
        
        LambdaQueryWrapper<Permission> disabledWrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getStatus, 1)
                .eq(Permission::getDeleted, false);
        statistics.put("disabledCount", permissionMapper.selectCount(disabledWrapper));

        return statistics;
    }

    @Override
    public boolean isOrganizationMember(Long userId, Long organizationId) {
        if (userId == null || organizationId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted()) {
            return false;
        }
        return Objects.equals(organizationId, user.getOrganizationId());
    }

    @Override
    public boolean isProjectMember(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId)
               .eq(ProjectMember::getStatus, 1)
               .isNull(ProjectMember::getDeletedAt);
        return projectMemberMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isProjectAdmin(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        if (isSuperAdmin(userId)) {
            return true;
        }

        Project project = projectMapper.selectById(projectId);
        if (project != null && !project.getDeleted() && Objects.equals(project.getOwnerId(), userId)) {
            return true;
        }

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
               .eq(ProjectMember::getUserId, userId)
               .eq(ProjectMember::getStatus, 1)
               .isNull(ProjectMember::getDeletedAt);
        if (!PROJECT_ADMIN_ROLE_IDS.isEmpty()) {
            wrapper.in(ProjectMember::getRoleId, PROJECT_ADMIN_ROLE_IDS);
        }
        return projectMemberMapper.selectOne(wrapper) != null;
    }

    @Override
    public boolean canAccessTask(Long userId, Long taskId) {
        if (userId == null || taskId == null) {
            return false;
        }
        Task task = taskMapper.selectById(taskId);
        if (task == null || task.getDeleted() || task.getProjectId() == null) {
            return false;
        }
        return isProjectMember(userId, task.getProjectId()) || isProjectAdmin(userId, task.getProjectId());
    }

    @Override
    public boolean canAccessDocument(Long userId, Long documentId) {
        if (userId == null || documentId == null) {
            return false;
        }
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted() || document.getProjectId() == null) {
            return false;
        }
        return isProjectMember(userId, document.getProjectId()) || isProjectAdmin(userId, document.getProjectId());
    }

    @Override
    public boolean canAccessNotification(Long userId, Long notificationId) {
        if (userId == null || notificationId == null) {
            return false;
        }
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || Boolean.TRUE.equals(notification.getDeleted())) {
            return false;
        }
        return Objects.equals(notification.getUserId(), userId);
    }

    @Override
    public boolean isOrganizationAdmin(Long userId, Long organizationId) {
        if (userId == null || organizationId == null) {
            return false;
        }
        if (isSuperAdmin(userId)) {
            return true;
        }
        Organization organization = organizationMapper.selectById(organizationId);
        if (organization == null || organization.getDeleted()) {
            return false;
        }
        return Objects.equals(organization.getCreatorId(), userId);
    }

    @Override
    public boolean canAccessChangeRequest(Long userId, Long changeRequestId) {
        if (userId == null || changeRequestId == null) {
            return false;
        }
        ChangeRequest changeRequest = changeRequestMapper.selectById(changeRequestId);
        if (changeRequest == null || changeRequest.getDeleted() || changeRequest.getProjectId() == null) {
            return false;
        }
        return isProjectMember(userId, changeRequest.getProjectId());
    }

    @Override
    public boolean canApproveChangeRequest(Long userId, Long changeRequestId) {
        if (userId == null || changeRequestId == null) {
            return false;
        }
        ChangeRequest changeRequest = changeRequestMapper.selectById(changeRequestId);
        if (changeRequest == null || changeRequest.getDeleted() || changeRequest.getProjectId() == null) {
            return false;
        }
        return isProjectAdmin(userId, changeRequest.getProjectId());
    }

    @Override
    public boolean canModifyUser(Long actorId, Long targetUserId) {
        if (actorId == null || targetUserId == null) {
            return false;
        }
        if (Objects.equals(actorId, targetUserId)) {
            return true;
        }
        return isSuperAdmin(actorId);
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> relations = userRoleMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(relations)) {
            return false;
        }

        List<Long> roleIds = relations.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }

        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>().in(Role::getId, roleIds));
        if (CollectionUtils.isEmpty(roles)) {
            return false;
        }

        return roles.stream()
                .filter(Objects::nonNull)
                .filter(role -> !role.getDeleted())
                .map(Role::getRoleCode)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .anyMatch(SUPER_ADMIN_ROLE_CODES::contains);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证权限编码唯一性
     *
     * @param permissionCode 权限编码
     * @param excludeId 排除的权限ID（更新时使用）
     */
    private void validatePermissionCodeUnique(String permissionCode, Long excludeId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionCode, permissionCode)
                .eq(Permission::getDeleted, false);
        
        if (excludeId != null) {
            wrapper.ne(Permission::getId, excludeId);
        }

        Long count = permissionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("权限编码已存在: " + permissionCode);
        }
    }

    /**
     * 验证权限是否存在
     *
     * @param permissionId 权限ID
     * @return 权限实体
     */
    private Permission validatePermissionExists(Long permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getDeleted()) {
            throw new BusinessException("权限不存在, permissionId=" + permissionId);
        }
        return permission;
    }

    /**
     * 检查是否会形成循环引用
     *
     * @param permissionId 权限ID
     * @param parentId 父级权限ID
     * @return 是否会形成循环引用
     */
    private boolean wouldCreateCircularReference(Long permissionId, Long parentId) {
        // 从parentId开始向上查找，看是否会遇到permissionId
        Long currentId = parentId;
        while (currentId != null && currentId > 0) {
            if (currentId.equals(permissionId)) {
                return true; // 找到了循环引用
            }
            
            Permission parent = permissionMapper.selectById(currentId);
            if (parent == null || parent.getDeleted()) {
                break;
            }
            
            currentId = parent.getParentId();
        }
        
        return false;
    }

    /**
     * 查询所有权限（内部方法，不使用缓存）
     *
     * @return 权限列表
     */
    private List<Permission> listAllPermissionsInternal() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, false)
                .orderByAsc(Permission::getSort)
                .orderByAsc(Permission::getCreateTime);

        return permissionMapper.selectList(wrapper);
    }

    /**
     * 构建权限树形结构
     *
     * @param allPermissions 所有权限列表
     * @param parentId 父级权限ID
     * @return 权限树节点列表
     */
    private List<PermissionTreeResponse> buildPermissionTree(List<Permission> allPermissions, Long parentId) {
        List<PermissionTreeResponse> tree = new ArrayList<>();

        for (Permission permission : allPermissions) {
            if (permission.getParentId().equals(parentId)) {
                PermissionTreeResponse node = convertToTreeResponse(permission);
                
                // 递归构建子节点
                List<PermissionTreeResponse> children = buildPermissionTree(allPermissions, permission.getId());
                if (!CollectionUtils.isEmpty(children)) {
                    node.setChildren(children);
                }
                
                tree.add(node);
            }
        }

        return tree;
    }

    /**
     * 将权限实体转换为响应DTO
     *
     * @param permission 权限实体
     * @return 权限响应DTO
     */
    private PermissionResponse convertToResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        BeanUtils.copyProperties(permission, response);
        return response;
    }

    /**
     * 将权限实体转换为树形结构响应DTO
     *
     * @param permission 权限实体
     * @return 权限树形结构响应DTO
     */
    private PermissionTreeResponse convertToTreeResponse(Permission permission) {
        PermissionTreeResponse response = new PermissionTreeResponse();
        BeanUtils.copyProperties(permission, response);
        return response;
    }
}
