package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层
 * <p>
 * 提供用户相关的数据库操作接口
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * <p>
     * 用于登录验证和用户信息查询
     * </p>
     *
     * @param username 用户名
     * @return User 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE username = #{username} AND deleted = false")
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * <p>
     * 用于邮箱验证和用户信息查询
     * </p>
     *
     * @param email 邮箱地址
     * @return User 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE email = #{email} AND deleted = false")
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * <p>
     * 用于手机号验证和用户信息查询
     * </p>
     *
     * @param phone 手机号
     * @return User 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE phone = #{phone} AND deleted = false")
    User findByPhone(@Param("phone") String phone);

    /**
     * 检查用户名是否存在
     * <p>
     * 用于注册时验证用户名唯一性
     * </p>
     *
     * @param username 用户名
     * @return int 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM tb_user WHERE username = #{username} AND deleted = false")
    int existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     * <p>
     * 用于注册时验证邮箱唯一性
     * </p>
     *
     * @param email 邮箱地址
     * @return int 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM tb_user WHERE email = #{email} AND deleted = false")
    int existsByEmail(@Param("email") String email);
}