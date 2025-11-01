package com.promanage.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.dto.PasswordStrengthResponse;
import com.promanage.service.service.IPasswordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 密码服务实现类
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-02
 */
@Slf4j
@Service
public class PasswordServiceImpl implements IPasswordService {

  // 密码强度检查正则表达式
  private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
  private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
  private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d");
  private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

  // 最低密码要求
  private static final int MIN_LENGTH = 6;
  private static final int RECOMMENDED_LENGTH = 8;

  @Override
  public PasswordStrengthResponse checkPasswordStrength(String password) {
    log.debug("检查密码强度, length={}", password != null ? password.length() : 0);

    if (password == null || password.isEmpty()) {
      return PasswordStrengthResponse.builder()
          .level("weak")
          .percent(0)
          .text("弱")
          .meetsRequirements(false)
          .suggestion("密码不能为空")
          .build();
    }

    int score = 0;
    List<String> suggestions = new ArrayList<>();

    // 检查长度 (20分)
    if (password.length() >= RECOMMENDED_LENGTH) {
      score += 20;
    } else if (password.length() >= MIN_LENGTH) {
      score += 10;
      suggestions.add("建议密码长度至少8个字符");
    } else {
      suggestions.add("密码长度至少6个字符");
    }

    // 检查小写字母 (20分)
    if (LOWERCASE_PATTERN.matcher(password).find()) {
      score += 20;
    } else {
      suggestions.add("建议包含小写字母");
    }

    // 检查大写字母 (20分)
    if (UPPERCASE_PATTERN.matcher(password).find()) {
      score += 20;
    } else {
      suggestions.add("建议包含大写字母");
    }

    // 检查数字 (20分)
    if (NUMBER_PATTERN.matcher(password).find()) {
      score += 20;
    } else {
      suggestions.add("建议包含数字");
    }

    // 检查特殊字符 (20分)
    if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
      score += 20;
    } else {
      suggestions.add("建议包含特殊字符 (!@#$%^&* 等)");
    }

    // 确定强度等级
    String level;
    String text;
    if (score < 40) {
      level = "weak";
      text = "弱";
    } else if (score < 80) {
      level = "medium";
      text = "中";
    } else {
      level = "strong";
      text = "强";
    }

    // 是否满足最低要求 (至少6个字符)
    boolean meetsRequirements = password.length() >= MIN_LENGTH;

    // 构建建议文本
    String suggestion = suggestions.isEmpty() ? "密码强度良好" : String.join("；", suggestions);

    log.debug("密码强度检查完成, level={}, score={}", level, score);

    return PasswordStrengthResponse.builder()
        .level(level)
        .percent(score)
        .text(text)
        .meetsRequirements(meetsRequirements)
        .suggestion(suggestion)
        .build();
  }

  @Override
  public void validatePasswordStrength(String password, int minLength, int maxLength) {
    log.debug("验证密码强度, minLength={}, maxLength={}", minLength, maxLength);

    // 检查密码长度
    if (password == null || password.length() < minLength) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "密码长度至少" + minLength + "位");
    }

    if (password.length() > maxLength) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "密码长度不能超过" + maxLength + "位");
    }

    // 检查密码复杂度
    int complexity = 0;

    // 包含小写字母
    if (LOWERCASE_PATTERN.matcher(password).find()) {
      complexity++;
    }

    // 包含大写字母
    if (UPPERCASE_PATTERN.matcher(password).find()) {
      complexity++;
    }

    // 包含数字
    if (NUMBER_PATTERN.matcher(password).find()) {
      complexity++;
    }

    // 包含特殊字符
    if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
      complexity++;
    }

    // 至少包含3种类型的字符
    if (complexity < 3) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "密码必须包含大小写字母、数字、特殊字符中的至少3种");
    }

    // 检查常见弱密码
    String[] weakPasswords = {
      "password", "12345678", "qwerty123", "admin123", "Password123",
      "abc123456", "11111111", "00000000", "password1", "123456789"
    };

    String lowerPassword = password.toLowerCase();
    for (String weakPwd : weakPasswords) {
      if (lowerPassword.equals(weakPwd.toLowerCase())) {
        throw new BusinessException(ResultCode.PARAM_ERROR, "密码过于常见，请使用更强的密码");
      }
    }

    // 检查是否包含连续字符（如：123456, abcdef）
    if (hasSequentialChars(password)) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能包含过多连续字符");
    }

    // 检查是否包含重复字符（如：aaaaaa, 111111）
    if (hasRepeatingChars(password)) {
      throw new BusinessException(ResultCode.PARAM_ERROR, "密码不能包含过多重复字符");
    }

    log.debug("密码强度验证通过");
  }

  /**
   * 检查是否包含连续字符
   *
   * @param password 密码
   * @return true if has sequential chars
   */
  private boolean hasSequentialChars(String password) {
    int sequentialCount = 0;
    for (int i = 0; i < password.length() - 1; i++) {
      char current = password.charAt(i);
      char next = password.charAt(i + 1);

      if (next == current + 1) {
        sequentialCount++;
        if (sequentialCount >= 3) {
          return true;
        }
      } else {
        sequentialCount = 0;
      }
    }
    return false;
  }

  /**
   * 检查是否包含重复字符
   *
   * @param password 密码
   * @return true if has repeating chars
   */
  private boolean hasRepeatingChars(String password) {
    int repeatCount = 1;
    for (int i = 0; i < password.length() - 1; i++) {
      if (password.charAt(i) == password.charAt(i + 1)) {
        repeatCount++;
        if (repeatCount >= 4) {
          return true;
        }
      } else {
        repeatCount = 1;
      }
    }
    return false;
  }
}
