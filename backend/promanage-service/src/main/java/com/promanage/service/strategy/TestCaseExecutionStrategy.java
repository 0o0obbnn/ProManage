package com.promanage.service.strategy;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.domain.ResultCode;
import com.promanage.common.exception.BusinessException;
import com.promanage.service.entity.TestExecution;
import com.promanage.service.mapper.TestExecutionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例执行策略
 *
 * @author ProManage Team
 * @date 2025-10-22
 */
@Slf4j
@RequiredArgsConstructor
public class TestCaseExecutionStrategy {

    private final TestExecutionMapper testExecutionMapper;

    /**
     * 执行测试用例
     */
    public TestExecution executeTestCase(Long testCaseId, String executor, String result, String notes) {
        TestExecution execution = new TestExecution();
        execution.setTestCaseId(testCaseId);
        execution.setExecutor(executor);

        // Convert String result to Integer (0-通过, 1-失败, 2-阻塞, 3-跳过)
        Integer resultCode;
        switch (result.toUpperCase()) {
            case "PASSED":
            case "PASS":
                resultCode = 0;
                break;
            case "FAILED":
            case "FAIL":
                resultCode = 1;
                break;
            case "BLOCKED":
                resultCode = 2;
                break;
            case "SKIPPED":
            case "SKIP":
                resultCode = 3;
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR, "Invalid result: " + result);
        }
        execution.setResult(resultCode);
        execution.setNotes(notes);
        execution.setExecuteTime(LocalDateTime.now());
        execution.setCreateTime(LocalDateTime.now());

        testExecutionMapper.insert(execution);
        return execution;
    }

    /**
     * 获取测试用例的执行历史
     */
    public List<TestExecution> getExecutionHistory(Long testCaseId) {
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecution::getTestCaseId, testCaseId);
        wrapper.orderByDesc(TestExecution::getExecuteTime);
        return testExecutionMapper.selectList(wrapper);
    }

    /**
     * 获取测试用例的最后执行结果
     */
    public TestExecution getLastExecution(Long testCaseId) {
        LambdaQueryWrapper<TestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TestExecution::getTestCaseId, testCaseId);
        wrapper.orderByDesc(TestExecution::getExecuteTime);
        wrapper.last("LIMIT 1");
        return testExecutionMapper.selectOne(wrapper);
    }

    /**
     * 获取项目的测试执行统计
     */
    public long getExecutionCount(Long projectId) {
        // 这里需要根据项目ID查询相关的测试用例，然后统计执行次数
        // 简化实现，实际应该通过JOIN查询
        return testExecutionMapper.selectCount(null);
    }
}
