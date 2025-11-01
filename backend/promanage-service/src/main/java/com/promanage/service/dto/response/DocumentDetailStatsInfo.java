package com.promanage.service.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情统计信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailStatsInfo {

  /** 浏览次数 */
  private Integer viewCount;

  /** 点赞次数 */
  private Integer likeCount;

  /** 标签列表 */
  private List<String> tags;

  /** 是否为模板 */
  private Boolean template;

  /** 优先级 */
  private Integer priority;
}
