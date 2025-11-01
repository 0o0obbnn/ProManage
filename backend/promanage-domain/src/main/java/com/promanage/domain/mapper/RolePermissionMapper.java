package com.promanage.domain.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.domain.entity.RolePermission;

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
  @Delete("DELETE FROM tb_role_permission WHERE permission_id = #{permissionId}")
  int deleteByPermissionId(Long permissionId);

  /**
   * 根据角色ID删除角色权限关联记录
   *
   * @param roleId 角色ID
   * @return 删除记录数
   */
  @Delete("DELETE FROM tb_role_permission WHERE role_id = #{roleId}")
  int deleteByRoleId(Long roleId);

  /**
   * 批量插入角色权限关联记录
   *
   * @param rolePermissions 角色权限关联记录列表
   * @return 插入记录数
   */
  @Insert(
      "<script>"
          + "INSERT INTO tb_role_permission (role_id, permission_id, create_time) VALUES "
          + "<foreach collection='list' item='item' separator=','>"
          + "(#{item.roleId}, #{item.permissionId}, #{item.createTime})"
          + "</foreach>"
          + "</script>")
  int batchInsert(@Param("list") List<RolePermission> rolePermissions);

  /**
   * 批量插入角色权限关联记录
   *
   * @param rolePermissions 角色权限关联记录列表
   * @return 插入记录数
   */
  default int insertBatch(List<RolePermission> rolePermissions) {
    return batchInsert(rolePermissions);
  }

  /**
   * 根据角色ID查询权限ID列表
   *
   * @param roleId 角色ID
   * @return 权限ID列表
   */
  @Select("SELECT permission_id FROM tb_role_permission WHERE role_id = #{roleId}")
  List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

  /**
   * 根据角色ID查询权限编码列表
   *
   * @param roleId 角色ID
   * @return 权限编码列表
   */
  @Select(
      "SELECT p.permission_code FROM tb_role_permission rp JOIN tb_permission p ON rp.permission_id = p.id WHERE rp.role_id = #{roleId}")
  List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);
}
