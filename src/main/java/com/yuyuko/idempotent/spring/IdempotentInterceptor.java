package com.yuyuko.idempotent.spring;

import com.yuyuko.idempotent.DenyException;
import com.yuyuko.idempotent.api.*;
import com.yuyuko.idempotent.parameters.MethodIdempotentEvaluationContext;
import com.yuyuko.idempotent.annotation.Idempotent;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public class IdempotentInterceptor implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentInterceptor.class);


    private IdempotentTemplate idempotentTemplate;

    public IdempotentInterceptor(IdempotentTemplate idempotentTemplate) {
        this.idempotentTemplate = idempotentTemplate;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Class<?> targetClass = (methodInvocation.getThis() != null ?
                AopUtils.getTargetClass(methodInvocation.getThis()) : null);
        Method specificMethod = ClassUtils.getMostSpecificMethod(methodInvocation.getMethod(),
                targetClass);
        final Method method = BridgeMethodResolver.findBridgedMethod(specificMethod);

        final Idempotent idempotentAnnotation = getAnnotation(method, Idempotent.class);
        if (idempotentAnnotation != null) {
            try {
                return handleIdempotent(method, methodInvocation, idempotentAnnotation);
            } catch (DenyException ex) {
                logger.info("拒绝执行方法[{}],幂等操作id[{}]", method.getName(), ex.getId());
                return null;
            }
        }
        return methodInvocation.proceed();
    }

    private Object handleIdempotent(Method method,
                                    MethodInvocation methodInvocation,
                                    Idempotent idempotentAnnotation) throws Throwable {
        Object[] args = methodInvocation.getArguments();
        return idempotentTemplate.execute(new AbstractIdempotentExecutor(method, args,
                idempotentAnnotation) {
            @Override
            public Object execute() throws Throwable {
                return methodInvocation.proceed();
            }
        });
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T> clazz) {
        return method == null ? null : method.getAnnotation(clazz);
    }
}
