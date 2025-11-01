package com.promanage.service.service;

/**
 * 邮件服务接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
public interface IEmailService {

  /**
   * 发送简单文本邮件
   *
   * @param to 收件人邮箱
   * @param subject 邮件主题
   * @param content 邮件内容
   */
  void sendSimpleEmail(String to, String subject, String content);

  /**
   * 发送HTML邮件
   *
   * @param to 收件人邮箱
   * @param subject 邮件主题
   * @param htmlContent HTML内容
   */
  void sendHtmlEmail(String to, String subject, String htmlContent);

  /**
   * 发送密码重置验证码邮件
   *
   * @param to 收件人邮箱
   * @param code 验证码
   */
  void sendPasswordResetCode(String to, String code);

  /**
   * 发送欢迎邮件
   *
   * @param to 收件人邮箱
   * @param username 用户名
   */
  void sendWelcomeEmail(String to, String username);
}
