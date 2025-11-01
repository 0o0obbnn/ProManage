package com.promanage.service.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.promanage.service.entity.ChangeRequest;

/**
 * 变更请求数据访问层
 *
 * <p>提供变更请求的数据库操作，继承MyBatis Plus的BaseMapper。
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-03
 */
@Mapper
public interface ChangeRequestMapper extends BaseMapper<ChangeRequest> {}
