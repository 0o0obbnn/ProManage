package com.promanage.service.impl;

import com.promanage.service.dto.PasswordStrengthResponse;
import com.promanage.service.service.IPasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
}
