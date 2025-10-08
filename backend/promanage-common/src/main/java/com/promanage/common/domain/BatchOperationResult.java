package com.promanage.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作结果
 * <p>
 * 用于返回批量操作的执行结果，包括成功数、失败数和失败详情
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResult<T> {
    
    /**
     * 总数
     */
    private int total;
    
    /**
     * 成功数
     */
    private int successCount;
    
    /**
     * 失败数
     */
    private int failureCount;
    
    /**
     * 成功的ID列表
     */
    @Builder.Default
    private List<T> successIds = new ArrayList<>();
    
    /**
     * 失败的详情列表
     */
    @Builder.Default
    private List<FailureDetail<T>> failures = new ArrayList<>();
    
    /**
     * 操作是否全部成功
     *
     * @return true表示全部成功
     */
    public boolean isAllSuccess() {
        return failureCount == 0;
    }
    
    /**
     * 操作是否全部失败
     *
     * @return true表示全部失败
     */
    public boolean isAllFailure() {
        return successCount == 0;
    }
    
    /**
     * 添加成功记录
     *
     * @param id 成功的ID
     */
    public void addSuccess(T id) {
        if (successIds == null) {
            successIds = new ArrayList<>();
        }
        successIds.add(id);
        successCount++;
    }
    
    /**
     * 添加失败记录
     *
     * @param id 失败的ID
     * @param reason 失败原因
     */
    public void addFailure(T id, String reason) {
        if (failures == null) {
            failures = new ArrayList<>();
        }
        failures.add(new FailureDetail<>(id, reason));
        failureCount++;
    }
    
    /**
     * 添加失败记录
     *
     * @param id 失败的ID
     * @param exception 异常信息
     */
    public void addFailure(T id, Exception exception) {
        addFailure(id, exception.getMessage());
    }
    
    /**
     * 失败详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureDetail<T> {
        /**
         * 失败的ID
         */
        private T id;
        
        /**
         * 失败原因
         */
        private String reason;
    }
    
    /**
     * 创建批量操作结果
     *
     * @param total 总数
     * @param <T> ID类型
     * @return 批量操作结果对象
     */
    public static <T> BatchOperationResult<T> create(int total) {
        return BatchOperationResult.<T>builder()
                .total(total)
                .successCount(0)
                .failureCount(0)
                .successIds(new ArrayList<>())
                .failures(new ArrayList<>())
                .build();
    }
}

