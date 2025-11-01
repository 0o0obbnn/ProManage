package com.promanage.common.enums;

import java.util.Arrays;

import lombok.Getter;

/**
 * 优先级枚举
 *
 * <p>定义任务、变更请求等实体的优先级
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-04
 */
@Getter
public enum Priority {

  /** 低优先级 */
  LOW(1, "低", "#52c41a"),

  /** 中等优先级 */
  MEDIUM(2, "中", "#1890ff"),

  /** 高优先级 */
  HIGH(3, "高", "#faad14"),

  /** 紧急 */
  URGENT(4, "紧急", "#f5222d");

  /** 优先级值 */
  private final int value;

  /** 优先级描述 */
  private final String description;

  /** 显示颜色（用于前端展示） */
  private final String color;

  Priority(int value, String description, String color) {
    this.value = value;
    this.description = description;
    this.color = color;
  }

  /**
   * 根据值获取枚举
   *
   * @param value 优先级值
   * @return 对应的枚举值，如果不存在则返回MEDIUM
   */
  public static Priority fromValue(Integer value) {
    if (value == null) {
      return MEDIUM; // 默认中等优先级
    }
    return Arrays.stream(values())
        .filter(priority -> priority.value == value)
        .findFirst()
        .orElse(MEDIUM);
  }

  /**
   * 检查是否是高优先级或紧急
   *
   * @return true表示是高优先级或紧急
   */
  public boolean isHighOrUrgent() {
    return this == HIGH || this == URGENT;
  }
}
