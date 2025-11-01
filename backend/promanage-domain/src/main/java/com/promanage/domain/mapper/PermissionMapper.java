package com.promanage.domain.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.domain.entity.Permission;

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
  @Select("SELECT * FROM tb_permission WHERE permission_code = #{permissionCode} AND deleted = false")
  Permission findByPermissionCode(String permissionCode);

  /**
   * 根据父级ID查找权限列表
   *
   * @param parentId 父级ID
   * @return 权限列表
   */
  @Select("SELECT * FROM tb_permission WHERE parent_id = #{parentId} AND deleted = false")
  List<Permission> findByParentId(Long parentId);

  /**
   * 根据角色ID查找权限列表
   *
   * @param roleId 角色ID
   * @return 权限列表
   */
  @Select(
      "SELECT p.* FROM tb_permission p "
          + "INNER JOIN tb_role_permission rp ON p.id = rp.permission_id "
          + "WHERE rp.role_id = #{roleId} AND p.deleted = false")
  List<Permission> findByRoleId(Long roleId);

  /**
   * 根据用户ID查找权限列表
   *
   * @param userId 用户ID
   * @return 权限列表
   */
  @Select(
      "SELECT DISTINCT p.* FROM tb_permission p "
          + "INNER JOIN tb_role_permission rp ON p.id = rp.permission_id "
          + "INNER JOIN tb_user_role ur ON rp.role_id = ur.role_id "
          + "WHERE ur.user_id = #{userId} AND p.deleted = false")
  List<Permission> findByUserId(Long userId);
}
