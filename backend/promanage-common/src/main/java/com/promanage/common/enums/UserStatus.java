package com.promanage.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

  /** 正常 */
  ACTIVE(1, "正常"),

  /** 禁用 */
  DISABLED(2, "禁用"),

  /** 锁定 */
  LOCKED(3, "锁定");

  /** 状态码 */
  private final Integer code;

  /** 状态描述 */
  private final String description;

  /**
   * 根据code获取枚举
   *
   * @param code 状态码
   * @return UserStatus
   */
  public static UserStatus getByCode(Integer code) {
    for (UserStatus status : values()) {
      if (status.getCode().equals(code)) {
        return status;
      }
    }
    return null;
  }
}
