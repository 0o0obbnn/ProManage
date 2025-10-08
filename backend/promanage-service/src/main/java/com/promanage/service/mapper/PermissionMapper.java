package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    /**
     * 根据权限编码查找权限
     * 
     * @param permissionCode 权限编码
     * @return 权限实体
     */
    @Select("SELECT * FROM permissions WHERE permission_code = #{permissionCode} AND deleted = false")
    Permission findByPermissionCode(String permissionCode);
    
    /**
     * 根据父级ID查找权限列表
     * 
     * @param parentId 父级ID
     * @return 权限列表
     */
    @Select("SELECT * FROM permissions WHERE parent_id = #{parentId} AND deleted = false")
    List<Permission> findByParentId(Long parentId);
    
    /**
     * 根据角色ID查找权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = false")
    List<Permission> findByRoleId(Long roleId);
    
    /**
     * 根据用户ID查找权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = false")
    List<Permission> findByUserId(Long userId);
}
