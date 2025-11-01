package com.promanage.service.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情时间信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailTimeInfo {

  /** 创建时间 */
  private LocalDateTime createTime;

  /** 更新时间 */
  private LocalDateTime updateTime;

  /** 发布时间 */
  private LocalDateTime publishedAt;
}
