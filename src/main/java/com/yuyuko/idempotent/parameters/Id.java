package com.yuyuko.idempotent.parameters;

import java.lang.annotation.*;

/**
 * 这个注解用于指明方法参数名，适用于低于jdk8的jdk版本
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Id {
    /**
     * 参数名
     * @return
     */
    String value();
}
