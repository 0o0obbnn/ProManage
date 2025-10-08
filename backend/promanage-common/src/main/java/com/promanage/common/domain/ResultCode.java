package com.promanage.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 * <p>
 * 定义系统中所有的响应码和对应的消息
 * </p>
 *
 * @author ProManage Team
 * @date 2025-09-30
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ========== 成功响应 ==========
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    /**
     * 请求参数错误
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权,请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "权限不足,禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "请求的资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 资源冲突
     */
    CONFLICT(409, "资源冲突"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁,请稍后再试"),

    // ========== 服务器错误 5xx ==========
    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    /**
     * 网关超时
     */
    GATEWAY_TIMEOUT(504, "网关超时"),

    // ========== 业务错误 1xxx ==========
    /**
     * 业务异常
     */
    BUSINESS_ERROR(1000, "业务处理失败"),

    /**
     * 参数校验失败
     */
    VALIDATION_ERROR(1001, "参数校验失败"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(1002, "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(1003, "数据已存在"),

    /**
     * 操作失败
     */
    OPERATION_FAILED(1004, "操作失败"),

    /**
     * 状态异常
     */
    INVALID_STATUS(1005, "当前状态不允许此操作"),

    // ========== 用户相关错误 2xxx ==========
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(2001, "用户不存在"),

    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIALS(2002, "用户名或密码错误"),

    /**
     * 用户已被禁用
     */
    USER_DISABLED(2003, "用户已被禁用"),

    /**
     * 用户已存在
     */
    USER_ALREADY_EXISTS(2004, "用户已存在"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(2005, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(2006, "邮箱已存在"),

    /**
     * 用户名已存在
     */
    USERNAME_ALREADY_EXISTS(2007, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_ALREADY_EXISTS(2008, "邮箱已存在"),

    /**
     * 不能删除自己
     */
    CANNOT_DELETE_SELF(2009, "不能删除自己的账号"),

    /**
     * 不能禁用自己
     */
    CANNOT_DISABLE_SELF(2010, "不能禁用自己的账号"),

    /**
     * Token无效
     */
    INVALID_TOKEN(2011, "Token无效或已过期"),

    /**
     * 密码不正确
     */
    WRONG_PASSWORD(2012, "密码不正确"),

    // ========== 文档相关错误 3xxx ==========
    /**
     * 文档不存在
     */
    DOCUMENT_NOT_FOUND(3001, "文档不存在"),

    /**
     * 文档已存在
     */
    DOCUMENT_EXISTS(3002, "文档已存在"),

    /**
     * 文档类型不支持
     */
    UNSUPPORTED_DOCUMENT_TYPE(3003, "文档类型不支持"),

    /**
     * 文件大小超限
     */
    FILE_SIZE_EXCEEDED(3004, "文件大小超过限制"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(3005, "文件上传失败"),

    /**
     * 文档正在被编辑
     */
    DOCUMENT_LOCKED(3006, "文档正在被其他用户编辑"),

    // ========== 项目相关错误 4xxx ==========
    /**
     * 项目不存在
     */
    PROJECT_NOT_FOUND(4001, "项目不存在"),

    /**
     * 项目名称已存在
     */
    PROJECT_NAME_EXISTS(4002, "项目名称已存在"),

    /**
     * 不是项目成员
     */
    NOT_PROJECT_MEMBER(4003, "您不是该项目成员"),

    /**
     * 项目已归档
     */
    PROJECT_ARCHIVED(4004, "项目已归档,不允许操作"),

    // ========== 权限相关错误 5xxx ==========
    /**
     * 无权限
     */
    NO_PERMISSION(5001, "您没有执行此操作的权限"),

    /**
     * 角色不存在
     */
    ROLE_NOT_FOUND(5002, "角色不存在"),

    /**
     * 权限不存在
     */
    PERMISSION_NOT_FOUND(5003, "权限不存在"),

    /**
     * 权限编码已存在
     */
    PERMISSION_CODE_ALREADY_EXISTS(5004, "权限编码已存在"),

    /**
     * 权限有子权限，无法删除
     */
    PERMISSION_HAS_CHILDREN(5005, "权限有子权限，无法删除"),

    // ========== 测试用例相关错误 6xxx ==========
    /**
     * 测试用例不存在
     */
    TEST_CASE_NOT_FOUND(6001, "测试用例不存在"),

    /**
     * 测试用例已存在
     */
    TEST_CASE_ALREADY_EXISTS(6002, "测试用例已存在");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}