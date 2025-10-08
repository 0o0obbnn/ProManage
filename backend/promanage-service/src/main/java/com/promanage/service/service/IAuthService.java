package com.promanage.service.service;

import com.promanage.common.entity.User;

/**
 * 认证服务接口
 * <p>
 * 提供用户登录、登出、令牌刷新等认证相关功能
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-01
 */
public interface IAuthService {

    /**
     * 用户认证
     * <p>
     * 验证用户名和密码，返回认证成功的用户信息
     * </p>
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User authenticate(String username, String password);

    /**
     * 更新最后登录信息
     * <p>
     * 记录用户最后登录时间和IP地址
     * </p>
     *
     * @param userId 用户ID
     * @param ipAddress IP地址
     */
    void updateLastLogin(Long userId, String ipAddress);

    /**
     * 用户注册
     * <p>
     * 创建新用户账号并分配默认角色
     * </p>
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @param email 电子邮箱
     * @param phone 手机号码（可选）
     * @param realName 真实姓名（可选）
     * @return 新创建的用户信息
     */
    User register(String username, String password, String email, String phone, String realName);

    /**
     * 发送密码重置验证码
     * <p>
     * 向用户邮箱发送6位数字验证码，用于重置密码
     * </p>
     *
     * @param email 电子邮箱
     */
    void sendPasswordResetCode(String email);

    /**
     * 验证密码重置验证码
     * <p>
     * 验证用户输入的验证码是否正确且未过期
     * </p>
     *
     * @param email 电子邮箱
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyResetCode(String email, String code);

    /**
     * 重置密码
     * <p>
     * 通过验证码重置用户密码
     * </p>
     *
     * @param email 电子邮箱
     * @param code 验证码
     * @param newPassword 新密码
     */
    void resetPassword(String email, String code, String newPassword);
}
