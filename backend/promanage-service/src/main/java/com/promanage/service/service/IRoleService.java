package com.promanage.service.service;

import com.promanage.common.result.PageResult;
import com.promanage.service.entity.Permission;
import com.promanage.service.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 * <p>
 * 提供角色管理的业务逻辑,包括角色的CRUD操作和权限分配
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface IRoleService {

    /**
     * 根据ID查询角色详情
     *
     * @param id 角色ID
     * @return 角色实体
     */
    Role getById(Long id);

    /**
     * 根据角色编码查询角色
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    Role getByRoleCode(String roleCode);

    /**
     * 分页查询角色列表
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    PageResult<Role> listRoles(Integer page, Integer pageSize, String keyword);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> listAll();

    /**
     * 创建角色
     *
     * @param role 角色实体
     * @return 角色ID
     */
    Long create(Role role);

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param role 角色实体
     */
    void update(Long id, Role role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 批量删除角色
     *
     * @param ids 角色ID列表
     * @return 删除的记录数
     */
    int batchDelete(List<Long> ids);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getRolePermissions(Long roleId);

    /**
     * 检查角色编码是否存在
     *
     * @param roleCode 角色编码
     * @return true表示存在
     */
    boolean existsByRoleCode(String roleCode);
}
