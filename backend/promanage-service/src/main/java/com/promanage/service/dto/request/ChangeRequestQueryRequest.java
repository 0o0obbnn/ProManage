package com.promanage.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变更请求查询请求DTO
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestQueryRequest {

    /** 项目ID */
    private Long projectId;

    /** 页码 */
    private Integer page;

    /** 每页大小 */
    private Integer pageSize;

    /** 关键词 */
    private String keyword;

    /** 状态 */
    private Integer status;

    /** 优先级 */
    private String priority;

    /** 请求人ID */
    private Long requesterId;

    /** 指派人ID */
    private Long assigneeId;

    /** 审批人ID */
    private Long approverId;

    /** 开始日期 */
    private String startDate;

    /** 结束日期 */
    private String endDate;
}
