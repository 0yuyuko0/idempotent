package com.yuyuko.idempotent.annotation;

import com.yuyuko.idempotent.api.IdempotentInfo;
import com.yuyuko.idempotent.parameters.Id;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Idempotent {
    /**
     * 幂等操作的唯一标识，使用spring el表达式 用#来引用方法参数，在低于jdk8的版本中使用@Id来代指参数
     * @see Id
     * @return  Spring-EL expression
     */
    String id();

    /**
     * 幂等操作最大执行时间
     */
    int maxExecutionTime() default IdempotentInfo.DEFAULT_MAX_EXECUTION_TIME;

    /**
     * 幂等持续时间，-1代表永不过期
     */
    int duration() default IdempotentInfo.DEFAULT_DURATION;

    /**
     * @see Idempotent#id()
     * id的前缀
     */
    String prefix() default IdempotentInfo.DEFAULT_PREFIX;

    /**
     * 会回滚的异常类
     *
     * @return
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 会回滚的异常类名
     *
     * @return
     */
    String[] rollbackForClassName() default {};

    /**
     * 不会回滚的异常类
     *
     * @return
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 不会回滚的异常类名
     *
     * @return
     */
    String[] noRollbackForClassName() default {};
}