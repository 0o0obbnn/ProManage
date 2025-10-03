package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 * <p>
 * 提供权限管理的业务逻辑,包括权限的CRUD操作和树形结构查询
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface IPermissionService {

    /**
     * 根据ID查询权限详情
     *
     * @param id 权限ID
     * @return 权限实体
     */
    Permission getById(Long id);

    /**
     * 根据权限编码查询权限
     *
     * @param permissionCode 权限编码
     * @return 权限实体
     */
    Permission getByPermissionCode(String permissionCode);

    /**
     * 分页查询权限列表
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    PageResult<Permission> listPermissions(Integer page, Integer pageSize, String keyword);

    /**
     * 查询所有权限
     *
     * @return 权限列表
     */
    List<Permission> listAll();

    /**
     * 查询权限树
     * <p>
     * 返回树形结构的权限列表
     * </p>
     *
     * @return 权限树列表
     */
    List<Permission> listTree();

    /**
     * 根据父级ID查询子权限
     *
     * @param parentId 父级权限ID
     * @return 子权限列表
     */
    List<Permission> listByParentId(Long parentId);

    /**
     * 创建权限
     *
     * @param permission 权限实体
     * @return 权限ID
     */
    Long create(Permission permission);

    /**
     * 更新权限
     *
     * @param id 权限ID
     * @param permission 权限实体
     */
    void update(Long id, Permission permission);

    /**
     * 删除权限
     *
     * @param id 权限ID
     */
    void delete(Long id);

    /**
     * 批量删除权限
     *
     * @param ids 权限ID列表
     * @return 删除的记录数
     */
    int batchDelete(List<Long> ids);

    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @return true表示存在
     */
    boolean existsByPermissionCode(String permissionCode);
}