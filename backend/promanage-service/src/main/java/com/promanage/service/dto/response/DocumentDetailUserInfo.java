package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情用户信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailUserInfo {

  /** 创建人ID */
  private Long creatorId;

  /** 创建人名称 */
  private String creatorName;

  /** 创建人头像 */
  private String creatorAvatar;

  /** 审核人ID */
  private Long reviewerId;

  /** 审核人名称 */
  private String reviewerName;

  /** 审核人头像 */
  private String reviewerAvatar;

  /** 更新人ID */
  private Long updaterId;

  /** 更新人名称 */
  private String updaterName;
}
