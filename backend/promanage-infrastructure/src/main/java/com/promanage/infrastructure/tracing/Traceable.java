package com.promanage.infrastructure.tracing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 追踪注解
 *
 * <p>用于标记需要追踪的方法
 *
 * @author ProManage Team
 * @version 1.0
 * @since 2025-10-16
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Traceable {
  String value() default "";

  String[] tags() default {};
}
