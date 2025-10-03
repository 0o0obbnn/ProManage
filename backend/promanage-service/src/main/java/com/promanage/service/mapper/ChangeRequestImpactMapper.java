package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.ChangeRequestImpact;
import org.apache.ibatis.annotations.Mapper;

/**
 * 变更请求影响分析数据访问层
 * <p>
 * 提供变更请求影响分析的数据库操作，继承MyBatis Plus的BaseMapper。
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface ChangeRequestImpactMapper extends BaseMapper<ChangeRequestImpact> {
}