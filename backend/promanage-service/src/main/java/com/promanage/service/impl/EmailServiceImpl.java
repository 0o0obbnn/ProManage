package com.promanage.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.promanage.service.service.IEmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件服务实现类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Value("${spring.application.name:ProManage}")
  private String applicationName;

  @Override
  @Async
  public void sendSimpleEmail(String to, String subject, String content) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);

      mailSender.send(message);
      log.info("发送简单邮件成功, to={}, subject={}", to, subject);
    } catch (MailException e) {
      log.error("发送简单邮件失败, to={}, subject={}", to, subject, e);
      throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
    }
  }

  @Override
  @Async
  public void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      log.info("发送HTML邮件成功, to={}, subject={}", to, subject);
    } catch (MessagingException e) {
      log.error("发送HTML邮件失败, to={}, subject={}", to, subject, e);
      throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
    }
  }

  @Override
  public void sendPasswordResetCode(String to, String code) {
    String subject = applicationName + " - 密码重置验证码";
    String htmlContent = buildPasswordResetEmailHtml(code);
    sendHtmlEmail(to, subject, htmlContent);
  }

  @Override
  public void sendWelcomeEmail(String to, String username) {
    String subject = "欢迎加入 " + applicationName;
    String htmlContent = buildWelcomeEmailHtml(username);
    sendHtmlEmail(to, subject, htmlContent);
  }

  /** 构建密码重置邮件HTML内容 */
  private String buildPasswordResetEmailHtml(String code) {
    return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .code-box { background: white; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                        .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }
                        .footer { text-align: center; color: #888; margin-top: 30px; font-size: 12px; }
                        .warning { color: #f56c6c; font-weight: bold; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                            <p>密码重置验证码</p>
                        </div>
                        <div class="content">
                            <p>您好，</p>
                            <p>您正在进行密码重置操作，您的验证码为：</p>
                            <div class="code-box">
                                <div class="code">%s</div>
                            </div>
                            <p>此验证码将在 <strong>5分钟</strong> 内有效，请尽快使用。</p>
                            <p class="warning">⚠️ 如果这不是您本人的操作，请忽略此邮件或立即联系管理员。</p>
                        </div>
                        <div class="footer">
                            <p>此邮件由系统自动发送，请勿直接回复。</p>
                            <p>&copy; 2025 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
        .formatted(applicationName, code, applicationName);
  }

  /** 构建欢迎邮件HTML内容 */
  private String buildWelcomeEmailHtml(String username) {
    return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; color: #888; margin-top: 30px; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>欢迎加入 %s</h1>
                        </div>
                        <div class="content">
                            <p>亲爱的 <strong>%s</strong>，</p>
                            <p>欢迎加入 %s 智能项目管理系统！</p>
                            <p>通过我们的平台，您可以：</p>
                            <ul>
                                <li>📋 统一知识库管理</li>
                                <li>🔄 智能变更管理</li>
                                <li>✅ 测试用例管理与复用</li>
                                <li>👥 高效团队协作</li>
                            </ul>
                            <p>现在开始您的项目管理之旅吧！</p>
                            <p style="text-align: center;">
                                <a href="#" class="button">立即登录</a>
                            </p>
                        </div>
                        <div class="footer">
                            <p>如有任何问题，请联系我们的技术支持团队。</p>
                            <p>&copy; 2025 %s. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
        .formatted(applicationName, username, applicationName, applicationName);
  }
}
