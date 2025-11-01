package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情文件信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailFileInfo {

  /** 文件URL */
  private String fileUrl;

  /** 文件大小 */
  private Long fileSize;

  /** 当前版本 */
  private String currentVersion;

  /** 文档内容 */
  private String content;

  /** 内容类型 */
  private String contentType;
}
