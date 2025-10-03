package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 项目详情响应DTO
 * <p>
 * 扩展项目响应，包含成员和文档信息
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "项目详情响应")
public class ProjectDetailResponse extends ProjectResponse {

    @Schema(description = "项目成员列表")
    private List<ProjectMemberResponse> members;

    @Schema(description = "最近更新的文档列表")
    private List<DocumentResponse> recentDocuments;

    @Schema(description = "项目统计信息")
    private ProjectStatistics statistics;
}

/**
 * 项目统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目统计信息")
class ProjectStatistics {

    @Schema(description = "总文档数", example = "50")
    private Integer totalDocuments;

    @Schema(description = "草稿文档数", example = "10")
    private Integer draftDocuments;

    @Schema(description = "已发布文档数", example = "35")
    private Integer publishedDocuments;

    @Schema(description = "总成员数", example = "15")
    private Integer totalMembers;

    @Schema(description = "活跃成员数", example = "12")
    private Integer activeMembers;
}