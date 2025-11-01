package com.promanage.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档详情基本信息
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailBasicInfo {

  /** 文档ID */
  private Long id;

  /** 文档标题 */
  private String title;

  /** 文档slug */
  private String slug;

  /** 文档摘要 */
  private String summary;

  /** 文档类型 */
  private String type;

  /** 文档状态 */
  private String status;

  /** 项目ID */
  private Long projectId;

  /** 项目名称 */
  private String projectName;

  /** 文件夹ID */
  private Long folderId;

  /** 文件夹名称 */
  private String folderName;
}
