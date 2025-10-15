package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档分类概要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档分类信息")
public class DocumentCategorySummary {

    @Schema(description = "分类ID", example = "12")
    private Long id;

    @Schema(description = "分类名称", example = "产品需求")
    private String name;
}
