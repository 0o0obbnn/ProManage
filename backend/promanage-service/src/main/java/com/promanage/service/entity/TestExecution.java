package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.promanage.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试执行历史实体类
 * <p>
 * 记录测试用例的执行历史，包括执行结果、环境、时间等信息
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_test_execution")
@Schema(description = "测试执行历史实体")
public class TestExecution extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 测试用例ID (不能为空)
     */
    @Schema(description = "测试用例ID", example = "1", required = true)
    private Long testCaseId;

    /**
     * 执行人ID (不能为空)
     */
    @Schema(description = "执行人ID", example = "1", required = true)
    private Long executorId;

    /**
     * 执行结果
     * <ul>
     *   <li>0 - 通过</li>
     *   <li>1 - 失败</li>
     *   <li>2 - 阻塞</li>
     *   <li>3 - 跳过</li>
     * </ul>
     */
    @Schema(description = "执行结果 (0-通过, 1-失败, 2-阻塞, 3-跳过)", example = "0", required = true)
    private Integer result;

    /**
     * 实际结果
     * <p>
     * 测试执行后的实际结果描述
     * </p>
     */
    @Schema(description = "实际结果", example = "登录成功，跳转到首页")
    private String actualResult;

    /**
     * 失败原因
     * <p>
     * 当执行结果为失败时，记录失败原因
     * </p>
     */
    @Schema(description = "失败原因", example = "登录按钮无响应")
    private String failureReason;

    /**
     * 执行时长（分钟）
     */
    @Schema(description = "执行时长（分钟）", example = "10")
    private Integer executionTime;

    /**
     * 执行环境
     * <p>
     * 测试执行的环境信息
     * </p>
     */
    @Schema(description = "执行环境", example = "Windows 10, Chrome 90")
    private String executionEnvironment;

    /**
     * 备注
     * <p>
     * 执行过程中的备注信息
     * </p>
     */
    @Schema(description = "备注", example = "浏览器版本升级后测试")
    private String notes;

    /**
     * 附件URL
     * <p>
     * 附件列表，JSON格式存储多个URL
     * </p>
     */
    @Schema(description = "附件URL (JSON格式)", example = "[\"http://example.com/screenshot1.png\"]")
    private String attachments;
}
