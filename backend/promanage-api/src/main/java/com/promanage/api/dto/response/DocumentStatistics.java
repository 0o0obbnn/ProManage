package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档统计信息
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-09-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档统计信息")
public class DocumentStatistics {

    @Schema(description = "总版本数", example = "5")
    private Integer totalVersions;

    @Schema(description = "总浏览量", example = "150")
    private Integer totalViews;

    @Schema(description = "本周浏览量", example = "25")
    private Integer weekViews;

    @Schema(description = "收藏数", example = "8")
    private Integer favoriteCount;

    @Schema(description = "评论数", example = "12")
    private Integer commentCount;
}