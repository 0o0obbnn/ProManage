package com.promanage.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.promanage.service.dto.request.AssignPermissionsRequest;
import com.promanage.service.dto.request.CreatePermissionRequest;
import com.promanage.service.dto.request.UpdatePermissionRequest;
import com.promanage.service.dto.response.PermissionResponse;
import com.promanage.service.dto.response.PermissionTreeResponse;
import com.promanage.service.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
public interface IPermissionService extends IService<Permission> {

    /**
     * 创建权限
     *
     * @param request 创建权限请求
     * @return 权限ID
     */
    Long createPermission(CreatePermissionRequest request);

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param request 更新权限请求
     * @return 是否成功
     */
    Boolean updatePermission(Long id, UpdatePermissionRequest request);

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 是否成功
     */
    Boolean deletePermission(Long id);

    /**
     * 根据ID获取权限详情
     *
     * @param id 权限ID
     * @return 权限详情
     */
    PermissionResponse getPermissionById(Long id);

    /**
     * 获取所有权限列表
     *
     * @return 权限列表
     */
    List<PermissionResponse> listAllPermissions();

    /**
     * 根据类型获取权限列表
     *
     * @param type 权限类型
     * @return 权限列表
     */
    List<PermissionResponse> listPermissionsByType(String type);

    /**
     * 获取权限树形结构
     *
     * @return 权限树
     */
    List<PermissionTreeResponse> getPermissionTree();

    /**
     * 为角色分配权限
     *
     * @param request 分配权限请求
     * @return 是否成功
     */
    Boolean assignPermissionsToRole(AssignPermissionsRequest request);

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionResponse> getRolePermissions(Long roleId);

    /**
     * 获取用户的权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<PermissionResponse> getUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否拥有权限
     */
    Boolean checkUserPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有权限查看权限列表
     *
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionViewPermission(Long userId);

    /**
     * 检查用户是否有权限创建权限
     *
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionCreatePermission(Long userId);

    /**
     * 检查用户是否有权限编辑权限
     *
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionEditPermission(Long userId);

    /**
     * 检查用户是否有权限删除权限
     *
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionDeletePermission(Long userId);

    /**
     * 检查用户是否有权限分配权限
     *
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionAssignPermission(Long userId);

    /**
     * 获取权限统计信息
     *
     * @return 统计信息
     */
    Object getPermissionStatistics();
}
