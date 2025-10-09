package com.promanage.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.promanage.service.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试用例Mapper接口
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-09
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {
}
