package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情关联信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailRelationInfo {

  /** 版本列表 */
  private java.util.List<DocumentVersionDTO> versions;

  /** 统计信息 */
  private DocumentStatisticsDTO statistics;
}
