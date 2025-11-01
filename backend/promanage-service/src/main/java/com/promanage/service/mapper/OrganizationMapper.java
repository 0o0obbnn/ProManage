package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.common.entity.Organization;

/**
 * 组织数据访问层
 *
 * <p>提供组织相关的数据库操作接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-06
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

  /**
   * 根据组织标识符查询组织
   *
   * <p>根据slug查找组织信息
   *
   * @param slug 组织标识符
   * @return Organization 组织实体，如果不存在返回null
   */
  @Select("SELECT * FROM organizations WHERE slug = #{slug} AND deleted_at IS NULL")
  Organization findBySlug(@Param("slug") String slug);

  /**
   * 检查组织标识符是否存在
   *
   * <p>用于创建组织时的标识符唯一性验证
   *
   * @param slug 组织标识符
   * @return true表示存在，false表示不存在
   */
  @Select("SELECT COUNT(1) FROM organizations WHERE slug = #{slug} AND deleted_at IS NULL")
  boolean existsBySlug(@Param("slug") String slug);

  /**
   * 根据创建者ID查询组织列表
   *
   * <p>查询指定用户创建的所有组织
   *
   * @param createdBy 创建者ID
   * @return List<Organization> 组织列表
   */
  @Select(
      "SELECT * FROM organizations WHERE created_by = #{createdBy} AND deleted_at IS NULL ORDER BY created_at DESC")
  List<Organization> findByCreatedBy(@Param("createdBy") Long createdBy);

  /**
   * 根据激活状态查询组织列表
   *
   * <p>查询指定状态的所有组织
   *
   * @param isActive 是否激活
   * @return List<Organization> 组织列表
   */
  @Select(
      "SELECT * FROM organizations WHERE is_active = #{isActive} AND deleted_at IS NULL ORDER BY created_at DESC")
  List<Organization> findByActiveStatus(@Param("isActive") Boolean isActive);

  /**
   * 根据订阅计划查询组织列表
   *
   * <p>查询指定订阅计划的所有组织
   *
   * @param subscriptionPlan 订阅计划
   * @return List<Organization> 组织列表
   */
  @Select(
      "SELECT * FROM organizations WHERE subscription_plan = #{subscriptionPlan} AND deleted_at IS NULL ORDER BY created_at DESC")
  List<Organization> findBySubscriptionPlan(@Param("subscriptionPlan") String subscriptionPlan);

  /**
   * 统计组织数量
   *
   * <p>统计系统中的组织总数
   *
   * @return int 组织数量
   */
  @Select("SELECT COUNT(1) FROM organizations WHERE deleted_at IS NULL")
  int countAll();

  /**
   * 统计激活状态的组织数量
   *
   * <p>统计指定激活状态的组织数量
   *
   * @param isActive 是否激活
   * @return int 组织数量
   */
  @Select("SELECT COUNT(1) FROM organizations WHERE is_active = #{isActive} AND deleted_at IS NULL")
  int countByActiveStatus(@Param("isActive") Boolean isActive);

  /**
   * 根据名称模糊查询组织
   *
   * <p>根据组织名称进行模糊搜索
   *
   * @param name 组织名称关键字
   * @return List<Organization> 组织列表
   */
  @Select(
      "SELECT * FROM organizations WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted_at IS NULL ORDER BY created_at DESC")
  List<Organization> findByNameLike(@Param("name") String name);

  /**
   * 根据订阅计划统计组织数量
   *
   * <p>统计指定订阅计划的组织数量
   *
   * @param subscriptionPlan 订阅计划
   * @return int 组织数量
   */
  @Select(
      "SELECT COUNT(1) FROM organizations WHERE subscription_plan = #{subscriptionPlan} AND deleted_at IS NULL")
  int countBySubscriptionPlan(@Param("subscriptionPlan") String subscriptionPlan);

  /**
   * 查找即将过期的订阅组织
   *
   * <p>查找订阅即将在未来指定天数内过期的组织
   *
   * @param days 天数
   * @return List<Organization> 组织列表
   */
  @Select(
      """
            SELECT *
            FROM organizations
            WHERE deleted_at IS NULL
              AND subscription_expires_at <= CURRENT_TIMESTAMP + (#{days} || ' days')::interval
              AND subscription_expires_at > CURRENT_TIMESTAMP
            """)
  List<Organization> findExpiringSubscriptions(@Param("days") Integer days);

  /**
   * 查找已过期的订阅组织
   *
   * <p>查找订阅已过期的组织
   *
   * @return List<Organization> 组织列表
   */
  @Select(
      "SELECT * FROM organizations WHERE subscription_expires_at <= CURRENT_TIMESTAMP AND deleted_at IS NULL")
  List<Organization> findExpiredSubscriptions();

  /**
   * 查找无订阅的组织
   *
   * <p>查找没有订阅计划或订阅计划为空的组织
   *
   * @return List<Organization> 组织列表
   */
  @Select(
      "SELECT * FROM organizations WHERE deleted_at IS NULL AND (subscription_plan IS NULL OR subscription_plan = '')")
  List<Organization> findOrganizationsWithoutSubscription();
}
