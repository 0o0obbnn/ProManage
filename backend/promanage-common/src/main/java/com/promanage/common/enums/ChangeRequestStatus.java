package com.promanage.common.enums;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

/**
 * 变更请求状态枚举
 *
 * <p>定义变更请求的所有可能状态及其允许的状态转换
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-04
 */
@Getter
public enum ChangeRequestStatus {

  /** 草稿 - 初始状态，可以编辑和删除 */
  DRAFT("DRAFT", "草稿", 0),

  /** 已提交 - 等待审批 */
  SUBMITTED("SUBMITTED", "已提交", 1),

  /** 待审批 - 等待审批人审批 */
  PENDING_APPROVAL("PENDING_APPROVAL", "待审批", 2),

  /** 审批中 - 正在审批流程中 */
  UNDER_REVIEW("UNDER_REVIEW", "审批中", 3),

  /** 已批准 - 审批通过，可以实施 */
  APPROVED("APPROVED", "已批准", 4),

  /** 已拒绝 - 审批未通过 */
  REJECTED("REJECTED", "已拒绝", 5),

  /** 已实施 - 变更已经实施完成 */
  IMPLEMENTED("IMPLEMENTED", "已实施", 6),

  /** 已关闭 - 变更请求已关闭 */
  CLOSED("CLOSED", "已关闭", 7);

  /** 状态代码 */
  private final String code;

  /** 状态描述 */
  private final String description;

  /** 状态顺序（用于排序和比较） */
  private final int order;

  ChangeRequestStatus(String code, String description, int order) {
    this.code = code;
    this.description = description;
    this.order = order;
  }

  /**
   * 根据代码获取枚举值
   *
   * @param code 状态代码
   * @return 对应的枚举值，如果不存在则返回null
   */
  public static ChangeRequestStatus fromCode(String code) {
    if (code == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(status -> status.code.equals(code))
        .findFirst()
        .orElse(null);
  }

  /**
   * 检查是否可以转换到目标状态
   *
   * @param target 目标状态
   * @return true表示可以转换，false表示不允许转换
   */
  public boolean canTransitionTo(ChangeRequestStatus target) {
    if (target == null) {
      return false;
    }

    // 定义状态转换规则
    switch (this) {
      case DRAFT:
        return target == SUBMITTED;
      case SUBMITTED:
        return target == PENDING_APPROVAL || target == UNDER_REVIEW;
      case PENDING_APPROVAL:
        return target == UNDER_REVIEW || target == APPROVED || target == REJECTED;
      case UNDER_REVIEW:
        return target == APPROVED || target == REJECTED;
      case APPROVED:
        return target == IMPLEMENTED;
      case REJECTED:
        return target == DRAFT;
      case IMPLEMENTED:
        return target == CLOSED;
      case CLOSED:
        return target == DRAFT; // 允许重新打开
      default:
        return false;
    }
  }

  /**
   * 获取允许转换的目标状态列表
   *
   * @return 允许转换的状态列表
   */
  public List<ChangeRequestStatus> getAllowedTransitions() {
    switch (this) {
      case DRAFT:
        return List.of(SUBMITTED);
      case SUBMITTED:
        return List.of(PENDING_APPROVAL, UNDER_REVIEW);
      case PENDING_APPROVAL:
        return List.of(UNDER_REVIEW, APPROVED, REJECTED);
      case UNDER_REVIEW:
        return List.of(APPROVED, REJECTED);
      case APPROVED:
        return List.of(IMPLEMENTED);
      case REJECTED:
        return List.of(DRAFT);
      case IMPLEMENTED:
        return List.of(CLOSED);
      case CLOSED:
        return List.of(DRAFT);
      default:
        return List.of();
    }
  }

  /**
   * 检查是否是终态
   *
   * @return true表示是终态，false表示不是
   */
  public boolean isFinalState() {
    return this == CLOSED || this == REJECTED;
  }

  /**
   * 检查是否可以编辑
   *
   * @return true表示可以编辑，false表示不可以
   */
  public boolean isEditable() {
    return this == DRAFT;
  }

  /**
   * 检查是否可以删除
   *
   * @return true表示可以删除，false表示不可以
   */
  public boolean isDeletable() {
    return this == DRAFT;
  }
}
