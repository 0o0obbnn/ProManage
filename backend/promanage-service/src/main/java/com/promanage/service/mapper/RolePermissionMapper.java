package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 * <p>
 * 提供角色权限关联数据访问方法,管理角色和权限的多对多关系
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID删除所有权限关联
     * <p>
     * 用于重新分配角色权限时先清空现有权限
     * </p>
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除所有角色关联
     * <p>
     * 用于删除权限时清理关联关系
     * </p>
     *
     * @param permissionId 权限ID
     * @return 删除的记录数
     */
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     * <p>
     * 用于为角色批量分配权限
     * </p>
     *
     * @param rolePermissions 角色权限关联列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<RolePermission> rolePermissions);

    /**
     * 检查角色是否拥有指定权限
     * <p>
     * 用于权限验证
     * </p>
     *
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return true表示拥有,false表示不拥有
     */
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}