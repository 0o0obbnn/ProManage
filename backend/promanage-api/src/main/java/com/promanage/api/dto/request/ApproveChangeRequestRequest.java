package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审批变更请求请求DTO
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Data
@Schema(description = "审批变更请求请求")
public class ApproveChangeRequestRequest {

    @NotBlank(message = "审批决定不能为空")
    @Schema(description = "审批决定 (APPROVED-批准, REJECTED-拒绝)", example = "APPROVED", requiredMode = Schema.RequiredMode.REQUIRED)
    private String decision;

    @Size(max = 1000, message = "审批意见长度不能超过1000个字符")
    @Schema(description = "审批意见", example = "界面设计合理，建议按照此方案实施")
    private String comments;
}