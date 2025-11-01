package com.promanage.domain.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
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
  @Delete("DELETE FROM tb_user_role WHERE user_id = #{userId}")
  int deleteByUserId(Long userId);

  /**
   * 根据用户ID和角色ID删除用户角色关联记录
   *
   * @param userId 用户ID
   * @param roleId 角色ID
   * @return 删除记录数
   */
  @Delete("DELETE FROM tb_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
  int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

  /**
   * 批量插入用户角色关联记录
   *
   * @param userRoles 用户角色关联记录列表
   * @return 插入记录数
   */
  @Insert(
      "<script>"
          + "INSERT INTO tb_user_role (user_id, role_id, create_time) VALUES "
          + "<foreach collection='list' item='item' separator=','>"
          + "(#{item.userId}, #{item.roleId}, #{item.createTime})"
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
  @Select("SELECT COUNT(1) > 0 FROM tb_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
  boolean existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

  /**
   * 根据用户ID查询角色编码列表
   *
   * @param userId 用户ID
   * @return 角色编码列表
   */
  @Select(
      "SELECT r.role_code FROM tb_user_role ur JOIN tb_role r ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
  List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

  /**
   * 根据用户ID查询角色ID列表
   *
   * @param userId 用户ID
   * @return 角色ID列表
   */
  @Select("SELECT role_id FROM tb_user_role WHERE user_id = #{userId}")
  List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

  /**
   * 根据用户ID查询用户角色关联记录列表
   *
   * @param userId 用户ID
   * @return 用户角色关联记录列表
   */
  @Select("SELECT * FROM tb_user_role WHERE user_id = #{userId}")
  List<UserRole> selectByUserId(@Param("userId") Long userId);

  /**
   * 根据角色ID查询用户ID列表
   *
   * @param roleId 角色ID
   * @return 用户ID列表
   */
  @Select("SELECT user_id FROM tb_user_role WHERE role_id = #{roleId}")
  List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);
}
