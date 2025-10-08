package com.promanage.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 测试用例详情响应DTO
 * <p>
 * 返回测试用例的详细信息，包括执行历史和评论
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试用例详情响应")
public class TestCaseDetailResponse extends TestCaseResponse {

    @Schema(description = "测试用例执行历史列表")
    private List<TestCaseExecutionHistoryResponse> executionHistory;

    @Schema(description = "测试用例评论列表")
    private List<TestCaseCommentResponse> comments;

    @Schema(description = "测试用例统计信息")
    private TestCaseStatistics statistics;

    /**
     * 测试用例执行历史响应
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "测试用例执行历史响应")
    public static class TestCaseExecutionHistoryResponse {

        @Schema(description = "执行历史ID", example = "1")
        private Long id;

        @Schema(description = "测试用例ID", example = "1")
        private Long testCaseId;

        @Schema(description = "执行结果 (PASS-通过, FAIL-失败, BLOCK-阻塞, SKIP-跳过)", example = "PASS")
        private String result;

        @Schema(description = "实际结果", example = "用户成功登录并跳转到首页，所有功能正常")
        private String actualResult;

        @Schema(description = "失败原因", example = "登录按钮点击无响应，控制台显示JavaScript错误")
        private String failureReason;

        @Schema(description = "实际执行时间（分钟）", example = "8")
        private Integer actualTime;

        @Schema(description = "执行环境", example = "Windows 10, Chrome 90.0.4430.212")
        private String executionEnvironment;

        @Schema(description = "执行备注", example = "测试过程中网络稍有延迟，但不影响测试结果")
        private String notes;

        @Schema(description = "执行人ID", example = "2")
        private Long executorId;

        @Schema(description = "执行人姓名", example = "李四")
        private String executorName;

        @Schema(description = "执行人头像", example = "https://example.com/avatar/user2.jpg")
        private String executorAvatar;

        @Schema(description = "执行时间", example = "2025-10-08T14:30:00")
        private LocalDateTime executedAt;

        @Schema(description = "附件URL列表")
        private List<String> attachments;
    }

    /**
     * 测试用例评论响应
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "测试用例评论响应")
    public static class TestCaseCommentResponse {

        @Schema(description = "评论ID", example = "1")
        private Long id;

        @Schema(description = "测试用例ID", example = "1")
        private Long testCaseId;

        @Schema(description = "评论内容", example = "这个测试用例很详细，覆盖了主要场景")
        private String content;

        @Schema(description = "评论人ID", example = "3")
        private Long commenterId;

        @Schema(description = "评论人姓名", example = "王五")
        private String commenterName;

        @Schema(description = "评论人头像", example = "https://example.com/avatar/user3.jpg")
        private String commenterAvatar;

        @Schema(description = "评论时间", example = "2025-10-07T16:20:00")
        private LocalDateTime commentedAt;

        @Schema(description = "是否为回复评论", example = "false")
        private Boolean isReply;

        @Schema(description = "父评论ID（如果是回复）", example = "0")
        private Long parentCommentId;
    }

    /**
     * 测试用例统计信息
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "测试用例统计信息")
    public static class TestCaseStatistics {

        @Schema(description = "总执行次数", example = "5")
        private Integer totalExecutions;

        @Schema(description = "通过次数", example = "3")
        private Integer passCount;

        @Schema(description = "失败次数", example = "1")
        private Integer failCount;

        @Schema(description = "阻塞次数", example = "0")
        private Integer blockCount;

        @Schema(description = "跳过次数", example = "1")
        private Integer skipCount;

        @Schema(description = "通过率（百分比）", example = "60.0")
        private Double passRate;

        @Schema(description = "平均执行时间（分钟）", example = "8.5")
        private Double averageExecutionTime;

        @Schema(description = "最后执行时间", example = "2025-10-08T14:30:00")
        private LocalDateTime lastExecutionTime;

        @Schema(description = "最后执行结果", example = "PASS")
        private String lastExecutionResult;
    }
}