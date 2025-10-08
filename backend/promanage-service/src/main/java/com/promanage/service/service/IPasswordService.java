package com.promanage.service.service;

import com.promanage.service.dto.PasswordStrengthResponse;

/**
 * 密码服务接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
public interface IPasswordService {

    /**
     * 检查密码强度
     *
     * @param password 密码
     * @return 密码强度信息
     */
    PasswordStrengthResponse checkPasswordStrength(String password);

    /**
     * 验证密码强度（抛出异常如果不符合要求）
     *
     * @param password 密码
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @throws com.promanage.common.exception.BusinessException 如果密码不符合要求
     */
    void validatePasswordStrength(String password, int minLength, int maxLength);
}
