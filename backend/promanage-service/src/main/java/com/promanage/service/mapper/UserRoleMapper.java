package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 * <p>
 * 提供用户角色关联数据访问方法,管理用户和角色的多对多关系
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID删除所有角色关联
     * <p>
     * 用于重新分配用户角色时先清空现有角色
     * </p>
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID删除所有用户关联
     * <p>
     * 用于删除角色时清理关联关系
     * </p>
     *
     * @param roleId 角色ID
     * @return 删除的记录数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     * <p>
     * 用于为用户批量分配角色
     * </p>
     *
     * @param userRoles 用户角色关联列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<UserRole> userRoles);

    /**
     * 检查用户是否拥有指定角色
     * <p>
     * 用于权限验证
     * </p>
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return true表示拥有,false表示不拥有
     */
    boolean existsByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}