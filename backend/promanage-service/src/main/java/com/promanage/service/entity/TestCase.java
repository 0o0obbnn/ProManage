package com.promanage.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import com.promanage.common.entity.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 测试用例实体类
 *
 * <p>存储测试用例的基本信息、步骤和预期结果 支持多种测试类型和优先级管理
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_test_case")
@Schema(description = "测试用例实体")
@SuppressWarnings("PMD.TooManyFields") // Entity类需要与数据库表结构保持一致
public class TestCase extends BaseEntity {

  private static final long serialVersionUID = 1L;

  /** 测试用例标题 (不能为空) */
  @Schema(description = "测试用例标题", example = "用户登录功能测试", required = true)
  private String title;

  /**
   * 测试用例描述
   *
   * <p>详细描述测试场景和目的
   */
  @Schema(description = "测试用例描述", example = "测试用户使用正确的用户名和密码登录系统")
  private String description;

  /**
   * 前置条件
   *
   * <p>执行测试前需要满足的条件
   */
  @Schema(description = "前置条件", example = "1. 用户已注册\n2. 系统正常运行")
  private String preconditions;

  /**
   * 测试步骤
   *
   * <p>详细的测试执行步骤，支持JSON格式存储结构化数据
   */
  @Schema(
      description = "测试步骤",
      example = "[{\"step\": 1, \"action\": \"打开登录页面\", \"expected\": \"页面正常显示\"}]")
  private String steps;

  /**
   * 预期结果
   *
   * <p>测试执行后期望得到的结果
   */
  @Schema(description = "预期结果", example = "用户成功登录并跳转到首页")
  private String expectedResult;

  /**
   * 实际结果
   *
   * <p>测试执行后的实际结果，用于记录测试执行情况
   */
  @Schema(description = "实际结果", example = "用户成功登录并跳转到首页")
  private String actualResult;

  /**
   * 测试用例类型 (不能为空)
   *
   * <ul>
   *   <li>FUNCTIONAL - 功能测试
   *   <li>PERFORMANCE - 性能测试
   *   <li>SECURITY - 安全测试
   *   <li>UI - 界面测试
   *   <li>INTEGRATION - 集成测试
   *   <li>REGRESSION - 回归测试
   *   <li>ACCEPTANCE - 验收测试
   * </ul>
   */
  @Schema(
      description = "测试用例类型 (FUNCTIONAL/PERFORMANCE/SECURITY/UI/INTEGRATION/REGRESSION/ACCEPTANCE)",
      example = "FUNCTIONAL",
      required = true)
  private String type;

  /**
   * 测试用例状态
   *
   * <ul>
   *   <li>0 - 草稿
   *   <li>1 - 待执行
   *   <li>2 - 执行中
   *   <li>3 - 通过
   *   <li>4 - 失败
   *   <li>5 - 阻塞
   *   <li>6 - 跳过
   * </ul>
   */
  @Schema(
      description = "测试用例状态 (0-草稿, 1-待执行, 2-执行中, 3-通过, 4-失败, 5-阻塞, 6-跳过)",
      example = "1",
      defaultValue = "0")
  private Integer status;

  /**
   * 优先级
   *
   * <p>1-低，2-中，3-高，4-紧急
   */
  @Schema(description = "优先级：1-低，2-中，3-高，4-紧急", example = "2")
  private Integer priority;

  /** 所属项目ID (不能为空) */
  @Schema(description = "所属项目ID", example = "1", required = true)
  private Long projectId;

  /**
   * 关联需求ID
   *
   * <p>关联的需求或功能点ID
   */
  @Schema(description = "关联需求ID", example = "10")
  private Long requirementId;

  /**
   * 关联任务ID
   *
   * <p>关联的开发任务ID
   */
  @Schema(description = "关联任务ID", example = "5")
  private Long taskId;

  /**
   * 模块名称
   *
   * <p>所属功能模块
   */
  @Schema(description = "模块名称", example = "用户管理")
  private String module;

  /**
   * 标签
   *
   * <p>用于分类和搜索的标签，多个标签用逗号分隔
   */
  @Schema(description = "标签", example = "登录,用户认证,核心功能")
  private String tags;

  /**
   * 指派人ID
   *
   * <p>负责执行测试用例的人员ID
   */
  @Schema(description = "指派人ID", example = "2")
  private Long assigneeId;

  /**
   * 审核人ID
   *
   * <p>负责审核测试用例的人员ID
   */
  @Schema(description = "审核人ID", example = "3")
  private Long reviewerId;

  /** 预估执行时间（分钟） */
  @Schema(description = "预估执行时间（分钟）", example = "10")
  private Integer estimatedTime;

  /** 实际执行时间（分钟） */
  @Schema(description = "实际执行时间（分钟）", example = "8")
  private Integer actualTime;

  /**
   * 执行环境
   *
   * <p>测试执行的环境信息
   */
  @Schema(description = "执行环境", example = "Windows 10, Chrome 90")
  private String executionEnvironment;

  /**
   * 测试数据
   *
   * <p>测试所需的数据，支持JSON格式
   */
  @Schema(
      description = "测试数据",
      example = "{\"username\": \"test@example.com\", \"password\": \"test123\"}")
  private String testData;

  /**
   * 失败原因
   *
   * <p>测试失败时的原因说明
   */
  @Schema(description = "失败原因", example = "登录按钮点击无响应")
  private String failureReason;

  /**
   * 严重程度
   *
   * <p>1-轻微，2-一般，3-严重，4-致命
   */
  @Schema(description = "严重程度：1-轻微，2-一般，3-严重，4-致命", example = "2")
  private Integer severity;

  /** 最后执行时间 */
  @Schema(description = "最后执行时间")
  private java.time.LocalDateTime lastExecutedAt;

  /** 最后执行人ID */
  @Schema(description = "最后执行人ID", example = "2")
  private Long lastExecutedById;

  /**
   * 版本号
   *
   * <p>测试用例的版本号，用于跟踪变更
   */
  @Schema(description = "版本号", example = "1.0")
  private String versionNumber;
}
