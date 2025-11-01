package com.promanage.service.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.dto.SearchResultDTO;
import com.promanage.service.entity.Project;

/**
 * 项目数据访问层
 *
 * <p>提供项目相关的数据库操作接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

  /**
   * 根据负责人ID查询项目列表
   *
   * <p>查询指定用户负责的所有项目
   *
   * @param ownerId 负责人ID
   * @return List<Project> 项目列表
   */
  @Select(
      "SELECT * FROM tb_project WHERE owner_id = #{ownerId} AND deleted = false ORDER BY create_time DESC")
  List<Project> findByOwnerId(@Param("ownerId") Long ownerId);

  /**
   * 根据项目状态查询项目列表
   *
   * <p>查询指定状态的所有项目
   *
   * @param status 项目状态
   * @return List<Project> 项目列表
   */
  @Select(
      "SELECT * FROM tb_project WHERE status = #{status} AND deleted = false ORDER BY create_time DESC")
  List<Project> findByStatus(@Param("status") Integer status);

  /**
   * 根据项目名称模糊查询
   *
   * <p>根据项目名称进行模糊搜索
   *
   * @param name 项目名称关键字
   * @return List<Project> 项目列表
   */
  @Select(
      "SELECT * FROM tb_project WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted = false ORDER BY create_time DESC")
  List<Project> findByNameLike(@Param("name") String name);

  /**
   * 统计用户负责的项目数量
   *
   * <p>统计指定用户负责的项目总数
   *
   * @param ownerId 负责人ID
   * @return int 项目数量
   */
  @Select("SELECT COUNT(1) FROM tb_project WHERE owner_id = #{ownerId} AND deleted = false")
  int countByOwnerId(@Param("ownerId") Long ownerId);

  /**
   * 统计指定状态的项目数量
   *
   * <p>统计指定状态的项目总数
   *
   * @param status 项目状态
   * @return int 项目数量
   */
  @Select("SELECT COUNT(1) FROM tb_project WHERE status = #{status} AND deleted = false")
  int countByStatus(@Param("status") Integer status);

  /**
   * 根据项目编码查找项目
   *
   * <p>用于项目编码的唯一性验证和项目查找
   *
   * @param code 项目编码
   * @return Project 项目实体,如果不存在返回null
   */
  @Select("SELECT * FROM tb_project WHERE code = #{code} AND deleted = false")
  Project findByCode(@Param("code") String code);

  /**
   * 根据成员ID查找项目列表
   *
   * <p>通过项目成员关联表查询用户参与的所有项目
   *
   * @param memberId 成员ID
   * @return List<Project> 项目列表
   */
  @Select(
      "SELECT p.* FROM tb_project p INNER JOIN tb_project_member pm ON p.id = pm.project_id WHERE pm.user_id = #{memberId} AND p.deleted = false AND pm.deleted = false ORDER BY p.create_time DESC")
  List<Project> findByMemberId(@Param("memberId") Long memberId);

  /**
   * 检查项目编码是否存在
   *
   * <p>用于创建项目时的编码唯一性验证
   *
   * @param code 项目编码
   * @return true表示存在,false表示不存在
   */
  @Select("SELECT COUNT(1) FROM tb_project WHERE code = #{code} AND deleted = false")
  boolean existsByCode(@Param("code") String code);

  /**
   * 搜索项目（用于搜索服务）
   *
   * @param params 搜索参数
   * @return 项目列表
   */
  List<Project> searchProjects(@Param("params") java.util.Map<String, Object> params);

  /**
   * 统计搜索项目数量
   *
   * @param params 搜索参数
   * @return 项目数量
   */
  long countSearchProjects(@Param("params") java.util.Map<String, Object> params);

  /**
   * 根据关键词获取不重复的项目名称
   *
   * @param keyword 关键词
   * @param limit 限制数量
   * @return 项目名称列表
   */
  List<String> getDistinctNamesByKeyword(
      @Param("keyword") String keyword, @Param("limit") Integer limit);

  /**
   * 搜索项目（用于搜索服务）
   *
   * @param keyword 关键词
   * @param offset 偏移量
   * @param limit 限制数量
   * @return 项目搜索结果列表
   */
  List<SearchResultDTO> searchProjects(
      @Param("keyword") String keyword,
      @Param("offset") Integer offset,
      @Param("limit") Integer limit);

  /**
   * 统计搜索项目数量
   *
   * @param keyword 关键词
   * @return 项目数量
   */
  Long countSearchProjects(@Param("keyword") String keyword);

  /**
   * 根据关键词获取不重复的项目名称（用于搜索建议）
   *
   * @param keyword 关键词
   * @return 项目名称列表
   */
  List<String> getDistinctNamesByKeyword(@Param("keyword") String keyword);
}
