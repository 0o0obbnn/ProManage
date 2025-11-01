package com.promanage.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 执行测试用例请求DTO
 *
 * <p>用于执行测试用例并记录执行结果
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@Schema(description = "执行测试用例请求")
public class ExecuteTestCaseRequest {

  /**
   * 执行结果
   *
   * <p>必填项，测试执行的结果状态
   */
  @NotBlank(message = "执行结果不能为空")
  @Schema(
      description = "执行结果 (PASS-通过, FAIL-失败, BLOCK-阻塞, SKIP-跳过)",
      example = "PASS",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String result;

  /**
   * 实际结果
   *
   * <p>可选项，测试执行后的实际结果
   */
  @Size(max = 1000, message = "实际结果长度不能超过1000个字符")
  @Schema(description = "实际结果", example = "用户成功登录并跳转到首页，所有功能正常")
  private String actualResult;

  /**
   * 失败原因
   *
   * <p>当执行结果为FAIL时，必须提供失败原因
   */
  @Schema(description = "失败原因", example = "登录按钮点击无响应，控制台显示JavaScript错误")
  private String failureReason;

  /**
   * 实际执行时间（分钟）
   *
   * <p>可选项，实际执行测试用例所用的时间
   */
  @Schema(description = "实际执行时间（分钟）", example = "8")
  private Integer actualTime;

  /**
   * 执行环境
   *
   * <p>可选项，测试执行的环境信息
   */
  @Size(max = 200, message = "执行环境长度不能超过200个字符")
  @Schema(description = "执行环境", example = "Windows 10, Chrome 90.0.4430.212")
  private String executionEnvironment;

  /**
   * 执行备注
   *
   * <p>可选项，执行过程中的额外说明
   */
  @Size(max = 500, message = "执行备注长度不能超过500个字符")
  @Schema(description = "执行备注", example = "测试过程中网络稍有延迟，但不影响测试结果")
  private String notes;

  /**
   * 附件URL列表
   *
   * <p>可选项，测试执行过程中的截图或其他附件
   */
  @Schema(
      description = "附件URL列表",
      example = "[\"https://example.com/screenshots/login_success.png\"]")
  private String[] attachments;
}
