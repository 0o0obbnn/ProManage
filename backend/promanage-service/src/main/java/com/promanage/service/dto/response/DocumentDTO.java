package com.promanage.service.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档响应DTO - 使用组合模式减少字段数量
 *
 * @author ProManage Team
 * @date 2025-10-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

  /** 文档基本信息 */
  private DocumentBasicInfo basicInfo;

  /** 文档项目信息 */
  private DocumentProjectInfo projectInfo;

  /** 文档用户信息 */
  private DocumentUserInfo userInfo;

  /** 文档文件信息 */
  private DocumentFileInfo fileInfo;

  /** 文档统计信息 */
  private DocumentStatsInfo statsInfo;

  /** 创建时间 */
  private LocalDateTime createTime;

  /** 更新时间 */
  private LocalDateTime updateTime;
}
