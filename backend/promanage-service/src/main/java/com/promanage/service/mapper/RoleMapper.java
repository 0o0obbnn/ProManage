package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 * <p>
 * 提供角色数据访问方法,支持角色的查询、创建、更新和删除
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查找角色列表
     * <p>
     * 通过用户角色关联表查询用户拥有的所有角色
     * </p>
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查找角色
     * <p>
     * 用于角色编码的唯一性验证和权限检查
     * </p>
     *
     * @param roleCode 角色编码 (例如: ROLE_ADMIN, ROLE_PM)
     * @return 角色实体,如果不存在返回null
     */
    Role findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 检查角色编码是否存在
     * <p>
     * 用于创建角色时的编码唯一性验证
     * </p>
     *
     * @param roleCode 角色编码
     * @return true表示存在,false表示不存在
     */
    boolean existsByRoleCode(@Param("roleCode") String roleCode);
}