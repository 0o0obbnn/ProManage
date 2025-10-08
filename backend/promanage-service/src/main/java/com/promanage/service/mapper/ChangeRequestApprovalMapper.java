package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.ChangeRequestApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 变更请求审批数据访问层
 * <p>
 * 提供变更请求审批历史的数据库操作
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface ChangeRequestApprovalMapper extends BaseMapper<ChangeRequestApproval> {

    /**
     * 根据变更请求ID查询审批历史
     * <p>
     * 按审批时间倒序排列
     * </p>
     *
     * @param changeRequestId 变更请求ID
     * @return 审批历史列表
     */
    List<ChangeRequestApproval> findByChangeRequestId(@Param("changeRequestId") Long changeRequestId);

    /**
     * 根据审批人ID查询审批记录
     *
     * @param approverId 审批人ID
     * @return 审批记录列表
     */
    List<ChangeRequestApproval> findByApproverId(@Param("approverId") Long approverId);

    /**
     * 统计变更请求的审批记录数量
     *
     * @param changeRequestId 变更请求ID
     * @return 审批记录数量
     */
    int countByChangeRequestId(@Param("changeRequestId") Long changeRequestId);
}

