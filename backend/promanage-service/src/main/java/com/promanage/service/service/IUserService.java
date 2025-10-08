package com.promanage.service.service;

import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.Permission;
import com.promanage.service.entity.Role;
import com.promanage.common.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 提供用户管理的业务逻辑,包括用户的CRUD操作、角色管理和权限检查
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
public interface IUserService {

    // ==================== 查询操作 ====================

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户实体
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在
     */
    User getById(Long id);

    /**
     * 批量根据ID查询用户
     * <p>
     * 用于避免N+1查询问题，一次性查询多个用户
     * </p>
     *
     * @param ids 用户ID列表
     * @return 用户映射表 (key: 用户ID, value: 用户实体)
     */
    java.util.Map<Long, User> getByIds(List<Long> ids);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体,如果不存在返回null
     */
    User getByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户实体,如果不存在返回null
     */
    User getByEmail(String email);

    /**
     * 分页查询用户列表
     *
     * @param page 页码 (从1开始)
     * @param pageSize 每页大小
     * @param keyword 搜索关键词 (可选,搜索用户名、邮箱、真实姓名)
     * @return 分页结果
     */
    PageResult<User> listUsers(Integer page, Integer pageSize, String keyword);

    /**
     * 查询所有用户 (不分页)
     * <p>
     * 谨慎使用,仅在数据量小时使用
     * </p>
     *
     * @return 用户列表
     */
    List<User> listAll();

    /**
     * 统计指定组织的用户数量
     * <p>
     * 仅统计未被删除的用户
     * </p>
     *
     * @param organizationId 组织ID
     * @return 用户数量
     */
    long countByOrganizationId(Long organizationId);

    // ==================== 创建操作 ====================

    /**
     * 创建用户
     * <p>
     * 会自动加密密码,验证用户名和邮箱唯一性
     * </p>
     *
     * @param user 用户实体 (需要设置username, password, email等必填字段)
     * @return 创建成功后的用户ID
     * @throws com.promanage.common.exception.BusinessException 如果用户名或邮箱已存在
     */
    Long create(User user);

    /**
     * 用户注册
     * <p>
     * 类似create但会分配默认角色
     * </p>
     *
     * @param user 用户实体
     * @return 创建成功后的用户ID
     * @throws com.promanage.common.exception.BusinessException 如果用户名或邮箱已存在
     */
    Long register(User user);

    // ==================== 更新操作 ====================

    /**
     * 更新用户信息
     * <p>
     * 不会更新密码和敏感字段
     * </p>
     *
     * @param id 用户ID
     * @param user 用户实体 (包含需要更新的字段)
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在
     */
    void update(Long id, User user);

    /**
     * 修改密码
     * <p>
     * 需要验证旧密码,会自动加密新密码
     * </p>
     *
     * @param id 用户ID
     * @param oldPassword 旧密码 (明文)
     * @param newPassword 新密码 (明文)
     * @throws com.promanage.common.exception.BusinessException 如果旧密码错误或用户不存在
     */
    void updatePassword(Long id, String oldPassword, String newPassword);

    /**
     * 重置密码 (管理员操作)
     * <p>
     * 不需要验证旧密码,直接设置新密码
     * </p>
     *
     * @param id 用户ID
     * @param newPassword 新密码 (明文)
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 状态值 (0-正常, 1-禁用, 2-锁定)
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在
     */
    void updateStatus(Long id, Integer status);

    /**
     * 更新最后登录信息
     * <p>
     * 记录用户登录时间和IP
     * </p>
     *
     * @param id 用户ID
     * @param ip 登录IP地址
     */
    void updateLastLogin(Long id, String ip);

    // ==================== 删除操作 ====================

    /**
     * 删除用户 (逻辑删除)
     *
     * @param id 用户ID
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在
     */
    void delete(Long id);

    /**
     * 批量删除用户 (逻辑删除)
     *
     * @param ids 用户ID列表
     * @return 删除的记录数
     */
    int batchDelete(List<Long> ids);

    // ==================== 角色管理 ====================

    /**
     * 为用户分配角色
     * <p>
     * 会先清空用户现有角色,再分配新角色
     * </p>
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在或角色不存在
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 为用户添加角色 (不清空现有角色)
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在或角色不存在
     */
    void addRole(Long userId, Long roleId);

    /**
     * 移除用户角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @throws com.promanage.common.exception.BusinessException 如果用户不存在
     */
    void removeRole(Long userId, Long roleId);

    /**
     * 查询用户的所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    // ==================== 权限检查 ====================

    /**
     * 查询用户的所有权限
     * <p>
     * 通过用户的角色汇总所有权限,自动去重
     * </p>
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(Long userId);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param userId 用户ID
     * @param permissionCode 权限编码 (例如: document:create)
     * @return true表示拥有权限,false表示没有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param roleCode 角色编码 (例如: ROLE_ADMIN)
     * @return true表示拥有角色,false表示没有角色
     */
    boolean hasRole(Long userId, String roleCode);

    // ==================== 验证操作 ====================

    /**
     * 验证用户密码
     *
     * @param userId 用户ID
     * @param password 密码 (明文)
     * @return true表示密码正确,false表示密码错误
     */
    boolean verifyPassword(Long userId, String password);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true表示存在,false表示不存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱地址
     * @return true表示存在,false表示不存在
     */
    boolean existsByEmail(String email);
}