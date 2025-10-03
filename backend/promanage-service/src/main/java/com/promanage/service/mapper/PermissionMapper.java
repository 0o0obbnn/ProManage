package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper接口
 * <p>
 * 提供权限数据访问方法,支持权限的查询、创建、更新和删除
 * 支持树形权限结构的查询
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查找权限列表
     * <p>
     * 通过角色权限关联表查询角色拥有的所有权限
     * </p>
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找权限列表
     * <p>
     * 通过用户角色关联表和角色权限关联表查询用户拥有的所有权限
     * 会自动去重
     * </p>
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> findByUserId(@Param("userId") Long userId);

    /**
     * 根据权限编码查找权限
     * <p>
     * 用于权限编码的唯一性验证和权限检查
     * </p>
     *
     * @param permissionCode 权限编码 (例如: document:create, project:view)
     * @return 权限实体,如果不存在返回null
     */
    Permission findByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 根据父级ID查找子权限列表
     * <p>
     * 用于构建权限树形结构
     * </p>
     *
     * @param parentId 父级权限ID,0表示顶级权限
     * @return 子权限列表
     */
    List<Permission> findByParentId(@Param("parentId") Long parentId);
}