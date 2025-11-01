package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import com.promanage.service.constant.CommonConstants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.domain.entity.UserRole;

/**
 * 用户角色关联Mapper接口
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

  /**
   * 根据用户ID删除用户角色关联记录
   *
   * @param userId 用户ID
   * @return 删除记录数
   */
  @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
  int deleteByUserId(Long userId);

  /**
   * 批量插入用户角色关联记录
   *
   * @param userRoles 用户角色关联记录列表
   * @return 插入记录数
   */
  @Insert(
      "<script>"
          + "INSERT INTO user_roles (user_id, role_id, created_at, updated_at) VALUES "
          + "<foreach collection='list' item='item' separator=','>"
          + "(#{item.userId}, #{item.roleId}, #{item.createdAt}, #{item.updatedAt})"
          + "</foreach>"
          + "</script>")
  int batchInsert(@Param("list") List<UserRole> userRoles);

  /**
   * 批量插入用户角色关联记录
   *
   * @param userRoles 用户角色关联记录列表
   * @return 插入记录数
   */
  default int insertBatch(List<UserRole> userRoles) {
    return batchInsert(userRoles);
  }

  /**
   * 检查用户角色关联关系是否存在
   *
   * @param userId 用户ID
   * @param roleId 角色ID
   * @return 是否存在
   */
  @Select("SELECT COUNT(1) > 0 FROM user_roles WHERE " + CommonConstants.USER_ID + " = #{" + CommonConstants.USER_ID + "} AND role_id = #{roleId}")
  boolean existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

  /**
   * 根据用户ID查询角色编码列表
   *
   * @param userId 用户ID
   * @return 角色编码列表
   */
  @Select(
      "SELECT r.code FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
  List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

  /**
   * 根据用户ID查询角色ID列表
   *
   * @param userId 用户ID
   * @return 角色ID列表
   */
  @Select("SELECT role_id FROM user_roles WHERE " + CommonConstants.USER_ID + " = #{" + CommonConstants.USER_ID + "}")
  List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

  /**
   * 根据用户ID查询用户角色关联记录列表
   *
   * @param userId 用户ID
   * @return 用户角色关联记录列表
   */
  @Select("SELECT * FROM user_roles WHERE " + CommonConstants.USER_ID + " = #{" + CommonConstants.USER_ID + "}")
  List<UserRole> selectByUserId(@Param("userId") Long userId);
}
