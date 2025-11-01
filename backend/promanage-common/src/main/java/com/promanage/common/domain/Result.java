package com.promanage.common.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一响应结果封装类
 *
 * <p>用于封装所有API接口的响应结果,提供统一的返回格式
 *
 * @param <T> 数据类型
 * @author ProManage Team
 * @date 2025-09-30
 */
@Data
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Result<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 响应码 */
  @Schema(description = "响应码", example = "200")
  private Integer code;

  /** 响应消息 */
  @Schema(description = "响应消息", example = "操作成功")
  private String message;

  /** 响应数据 */
  @Schema(description = "响应数据")
  private T data;

  /** 时间戳 */
  @Schema(description = "时间戳")
  private Long timestamp;

  /** 私有构造函数 */
  private Result() {
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * 成功响应 - 无数据
   *
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> success() {
    Result<T> result = new Result<>();
    result.setCode(ResultCode.SUCCESS.getCode());
    result.setMessage(ResultCode.SUCCESS.getMessage());
    return result;
  }

  /**
   * 成功响应 - 带数据
   *
   * @param data 响应数据
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> success(T data) {
    Result<T> result = new Result<>();
    result.setCode(ResultCode.SUCCESS.getCode());
    result.setMessage(ResultCode.SUCCESS.getMessage());
    result.setData(data);
    return result;
  }

  /**
   * 成功响应 - 自定义消息
   *
   * @param message 响应消息
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> success(String message) {
    Result<T> result = new Result<>();
    result.setCode(ResultCode.SUCCESS.getCode());
    result.setMessage(message);
    return result;
  }

  /**
   * 成功响应 - 自定义消息和数据
   *
   * @param message 响应消息
   * @param data 响应数据
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> success(String message, T data) {
    Result<T> result = new Result<>();
    result.setCode(ResultCode.SUCCESS.getCode());
    result.setMessage(message);
    result.setData(data);
    return result;
  }

  /**
   * 失败响应 - 默认错误
   *
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> error() {
    Result<T> result = new Result<>();
    result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getCode());
    result.setMessage(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    return result;
  }

  /**
   * 失败响应 - 自定义消息
   *
   * @param message 错误消息
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> error(String message) {
    Result<T> result = new Result<>();
    result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getCode());
    result.setMessage(message);
    return result;
  }

  /**
   * 失败响应 - 自定义错误码和消息
   *
   * @param code 错误码
   * @param message 错误消息
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> error(Integer code, String message) {
    Result<T> result = new Result<>();
    result.setCode(code);
    result.setMessage(message);
    return result;
  }

  /**
   * 失败响应 - 使用ResultCode
   *
   * @param resultCode 结果码枚举
   * @param <T> 数据类型
   * @return Result
   */
  public static <T> Result<T> error(ResultCode resultCode) {
    Result<T> result = new Result<>();
    result.setCode(resultCode.getCode());
    result.setMessage(resultCode.getMessage());
    return result;
  }

  /**
   * 判断是否成功
   *
   * @return true-成功, false-失败
   */
  public boolean isSuccess() {
    return ResultCode.SUCCESS.getCode().equals(this.code);
  }
}
