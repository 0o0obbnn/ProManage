package com.promanage.common.annotation;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * API版本控制注解
 *
 * <p>用于控制API的版本，支持在URL路径中包含版本号
 *
 * @author ProManage Team
 * @date 2025-10-08
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping
public @interface ApiVersion {

  /**
   * API版本号
   *
   * <p>例如: 1, 2, v1, v2
   */
  @AliasFor(annotation = RequestMapping.class, attribute = "path")
  String[] value() default {};

  /**
   * API版本路径
   *
   * <p>例如: "/v1", "/api/v1"
   */
  @AliasFor(annotation = RequestMapping.class, attribute = "path")
  String[] path() default {};

  /** HTTP请求方法 */
  @AliasFor(annotation = RequestMapping.class, attribute = "method")
  RequestMethod[] method() default {};

  /** 请求参数 */
  @AliasFor(annotation = RequestMapping.class, attribute = "params")
  String[] params() default {};

  /** 请求头 */
  @AliasFor(annotation = RequestMapping.class, attribute = "headers")
  String[] headers() default {};

  /** 消费的内容类型 */
  @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
  String[] consumes() default {};

  /** 生成的内容类型 */
  @AliasFor(annotation = RequestMapping.class, attribute = "produces")
  String[] produces() default {};
}
