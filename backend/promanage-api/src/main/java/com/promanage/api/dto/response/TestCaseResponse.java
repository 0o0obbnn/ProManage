package com.promanage.api.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 测试用例响应DTO
 *
 * <p>返回测试用例的基本信息
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-08
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试用例响应")
public class TestCaseResponse {

  @Schema(description = "测试用例ID", example = "1")
  private Long id;

  @Schema(description = "测试用例标题", example = "用户登录功能测试")
  private String title;

  @Schema(description = "测试用例描述", example = "测试用户使用正确的用户名和密码登录系统")
  private String description;

  @Schema(description = "前置条件", example = "1. 用户已注册\n2. 系统正常运行")
  private String preconditions;

  @Schema(description = "测试步骤", example = "1. 打开登录页面\n2. 输入正确的用户名\n3. 输入正确的密码\n4. 点击登录按钮")
  private String steps;

  @Schema(description = "预期结果", example = "用户成功登录并跳转到首页")
  private String expectedResult;

  @Schema(description = "实际结果", example = "用户成功登录并跳转到首页")
  private String actualResult;

  @Schema(
      description = "测试用例类型 (FUNCTIONAL/PERFORMANCE/SECURITY/UI/INTEGRATION/REGRESSION/ACCEPTANCE)",
      example = "FUNCTIONAL")
  private String type;

  @Schema(description = "测试用例状态 (0-草稿, 1-待执行, 2-执行中, 3-通过, 4-失败, 5-阻塞, 6-跳过)", example = "3")
  private Integer status;

  @Schema(description = "优先级：1-低，2-中，3-高，4-紧急", example = "2")
  private Integer priority;

  @Schema(description = "所属项目ID", example = "1")
  private Long projectId;

  @Schema(description = "所属项目名称", example = "ProManage系统开发")
  private String projectName;

  @Schema(description = "关联需求ID", example = "10")
  private Long requirementId;

  @Schema(description = "关联任务ID", example = "5")
  private Long taskId;

  @Schema(description = "关联任务标题", example = "实现用户登录功能")
  private String taskTitle;

  @Schema(description = "模块名称", example = "用户管理")
  private String module;

  @Schema(description = "标签", example = "登录,用户认证,核心功能")
  private String tags;

  @Schema(description = "创建人ID", example = "1")
  private Long creatorId;

  @Schema(description = "创建人姓名", example = "张三")
  private String creatorName;

  @Schema(description = "创建人头像", example = "https://example.com/avatar/user1.jpg")
  private String creatorAvatar;

  @Schema(description = "指派人ID", example = "2")
  private Long assigneeId;

  @Schema(description = "指派人姓名", example = "李四")
  private String assigneeName;

  @Schema(description = "指派人头像", example = "https://example.com/avatar/user2.jpg")
  private String assigneeAvatar;

  @Schema(description = "审核人ID", example = "3")
  private Long reviewerId;

  @Schema(description = "审核人姓名", example = "王五")
  private String reviewerName;

  @Schema(description = "审核人头像", example = "https://example.com/avatar/user3.jpg")
  private String reviewerAvatar;

  @Schema(description = "预估执行时间（分钟）", example = "10")
  private Integer estimatedTime;

  @Schema(description = "实际执行时间（分钟）", example = "8")
  private Integer actualTime;

  @Schema(description = "执行环境", example = "Windows 10, Chrome 90")
  private String executionEnvironment;

  @Schema(description = "测试数据", example = "用户名: test@example.com, 密码: test123")
  private String testData;

  @Schema(description = "失败原因", example = "登录按钮点击无响应")
  private String failureReason;

  @Schema(description = "严重程度：1-轻微，2-一般，3-严重，4-致命", example = "2")
  private Integer severity;

  @Schema(description = "最后执行时间", example = "2025-10-08T14:30:00")
  private LocalDateTime lastExecutedAt;

  @Schema(description = "最后执行人ID", example = "2")
  private Long lastExecutedById;

  @Schema(description = "最后执行人姓名", example = "李四")
  private String lastExecutedByName;

  @Schema(description = "版本号", example = "1.0")
  private String version;

  @Schema(description = "创建时间", example = "2025-10-01T10:00:00")
  private LocalDateTime createTime;

  @Schema(description = "更新时间", example = "2025-10-08T14:30:00")
  private LocalDateTime updateTime;

  @Schema(description = "评论数量", example = "3")
  private Integer commentCount;

  @Schema(description = "执行历史数量", example = "5")
  private Integer executionCount;
}
