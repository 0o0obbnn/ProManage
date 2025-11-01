package com.promanage.service.strategy;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.result.PageResult;
import com.promanage.service.entity.TestCase;
import com.promanage.service.mapper.TestCaseMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例查询策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class TestCaseQueryStrategy {

    private final TestCaseMapper testCaseMapper;

    /**
     * 分页查询测试用例
     */
    public PageResult<TestCase> getTestCases(Long projectId, String keyword, String status, 
                                           String priority, String type, int page, int pageSize) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProjectId, projectId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(TestCase::getTitle, keyword)
                .or()
                .like(TestCase::getDescription, keyword)
            );
        }
        
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(TestCase::getStatus, status);
        }
        
        if (priority != null && !priority.trim().isEmpty()) {
            wrapper.eq(TestCase::getPriority, priority);
        }
        
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(TestCase::getType, type);
        }
        
        wrapper.orderByDesc(TestCase::getCreateTime);
        
        IPage<TestCase> pageResult = testCaseMapper.selectPage(new Page<>(page, pageSize), wrapper);
        
        return PageResult.<TestCase>builder()
            .list(pageResult.getRecords())
            .total(pageResult.getTotal())
            .page(page)
            .pageSize(pageSize)
            .build();
    }

    /**
     * 根据ID获取测试用例
     */
    public TestCase getTestCaseById(Long id) {
        return testCaseMapper.selectById(id);
    }

    /**
     * 获取项目的测试用例列表
     */
    public List<TestCase> getTestCasesByProjectId(Long projectId) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProjectId, projectId);
        wrapper.orderByDesc(TestCase::getCreateTime);
        return testCaseMapper.selectList(wrapper);
    }

    /**
     * 获取测试用例统计信息
     */
    public long getTestCaseCount(Long projectId) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProjectId, projectId);
        return testCaseMapper.selectCount(wrapper);
    }

    /**
     * 获取按状态分组的测试用例数量
     */
    public long getTestCaseCountByStatus(Long projectId, String status) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestCase::getProjectId, projectId);
        wrapper.eq(TestCase::getStatus, status);
        return testCaseMapper.selectCount(wrapper);
    }
}
