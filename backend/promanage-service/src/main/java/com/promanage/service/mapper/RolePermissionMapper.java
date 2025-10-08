package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.RolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 根据权限ID删除角色权限关联记录
     * 
     * @param permissionId 权限ID
     * @return 删除记录数
     */
    @Delete("DELETE FROM role_permissions WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(Long permissionId);
    
    /**
     * 根据角色ID删除角色权限关联记录
     * 
     * @param roleId 角色ID
     * @return 删除记录数
     */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);
    
    /**
     * 批量插入角色权限关联记录
     * 
     * @param rolePermissions 角色权限关联记录列表
     * @return 插入记录数
     */
    @Insert("<script>" +
            "INSERT INTO role_permissions (role_id, permission_id, created_at, updated_at) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.roleId}, #{item.permissionId}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<RolePermission> rolePermissions);
}
