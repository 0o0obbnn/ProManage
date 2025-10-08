package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建测试用例请求DTO
 * <p>
 * 用于创建新的测试用例，包含测试用例的基本信息和测试步骤
 * </p>
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "创建测试用例请求")
public class CreateTestCaseRequest {

    /**
     * 测试用例标题
     * <p>
     * 必填项，长度限制为1-200个字符
     * </p>
     */
    @NotBlank(message = "测试用例标题不能为空")
    @Size(max = 200, message = "测试用例标题长度不能超过200个字符")
    @Schema(description = "测试用例标题", example = "用户登录功能测试", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 测试用例描述
     * <p>
     * 可选项，最大长度1000个字符
     * </p>
     */
    @Size(max = 1000, message = "测试用例描述长度不能超过1000个字符")
    @Schema(description = "测试用例描述", example = "测试用户使用正确的用户名和密码登录系统")
    private String description;

    /**
     * 前置条件
     * <p>
     * 可选项，执行测试前需要满足的条件
     * </p>
     */
    @Size(max = 1000, message = "前置条件长度不能超过1000个字符")
    @Schema(description = "前置条件", example = "1. 用户已注册\n2. 系统正常运行")
    private String preconditions;

    /**
     * 测试步骤
     * <p>
     * 必填项，详细的测试执行步骤
     * </p>
     */
    @NotBlank(message = "测试步骤不能为空")
    @Size(max = 2000, message = "测试步骤长度不能超过2000个字符")
    @Schema(description = "测试步骤", example = "1. 打开登录页面\n2. 输入正确的用户名\n3. 输入正确的密码\n4. 点击登录按钮", requiredMode = Schema.RequiredMode.REQUIRED)
    private String steps;

    /**
     * 预期结果
     * <p>
     * 必填项，测试执行后期望得到的结果
     * </p>
     */
    @NotBlank(message = "预期结果不能为空")
    @Size(max = 1000, message = "预期结果长度不能超过1000个字符")
    @Schema(description = "预期结果", example = "用户成功登录并跳转到首页", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expectedResult;

    /**
     * 测试用例类型
     * <p>
     * 必填项，测试用例的类型
     * </p>
     */
    @NotBlank(message = "测试用例类型不能为空")
    @Schema(description = "测试用例类型 (FUNCTIONAL/PERFORMANCE/SECURITY/UI/INTEGRATION/REGRESSION/ACCEPTANCE)", example = "FUNCTIONAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    /**
     * 优先级
     * <p>
     * 可选项，1-低，2-中（默认），3-高，4-紧急
     * </p>
     */
    @Schema(description = "优先级：1-低，2-中，3-高，4-紧急", example = "2")
    private Integer priority = 2;

    /**
     * 关联需求ID
     * <p>
     * 可选项，关联的需求或功能点ID
     * </p>
     */
    @Schema(description = "关联需求ID", example = "10")
    private Long requirementId;

    /**
     * 关联任务ID
     * <p>
     * 可选项，关联的开发任务ID
     * </p>
     */
    @Schema(description = "关联任务ID", example = "5")
    private Long taskId;

    /**
     * 模块名称
     * <p>
     * 可选项，所属功能模块
     * </p>
     */
    @Size(max = 100, message = "模块名称长度不能超过100个字符")
    @Schema(description = "模块名称", example = "用户管理")
    private String module;

    /**
     * 标签
     * <p>
     * 可选项，用于分类和搜索的标签，多个标签用逗号分隔
     * </p>
     */
    @Size(max = 200, message = "标签长度不能超过200个字符")
    @Schema(description = "标签", example = "登录,用户认证,核心功能")
    private String tags;

    /**
     * 指派人ID
     * <p>
     * 可选项，负责执行测试用例的人员ID
     * </p>
     */
    @Schema(description = "指派人ID", example = "2")
    private Long assigneeId;

    /**
     * 审核人ID
     * <p>
     * 可选项，负责审核测试用例的人员ID
     * </p>
     */
    @Schema(description = "审核人ID", example = "3")
    private Long reviewerId;

    /**
     * 预估执行时间（分钟）
     * <p>
     * 可选项，预估执行测试用例所需的时间
     * </p>
     */
    @Schema(description = "预估执行时间（分钟）", example = "10")
    private Integer estimatedTime;

    /**
     * 测试数据
     * <p>
     * 可选项，测试所需的数据
     * </p>
     */
    @Size(max = 1000, message = "测试数据长度不能超过1000个字符")
    @Schema(description = "测试数据", example = "用户名: test@example.com, 密码: test123")
    private String testData;

    /**
     * 严重程度
     * <p>
     * 可选项，1-轻微，2-一般（默认），3-严重，4-致命
     * </p>
     */
    @Schema(description = "严重程度：1-轻微，2-一般，3-严重，4-致命", example = "2")
    private Integer severity = 2;
}