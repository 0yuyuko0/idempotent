package com.yuyuko.idempotent.spring;

import com.yuyuko.idempotent.RejectedException;
import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.api.AbstractIdempotentExecutor;
import com.yuyuko.idempotent.api.IdempotentManager;
import com.yuyuko.idempotent.api.IdempotentTemplate;
import com.yuyuko.idempotent.expression.ExpressionResolver;
import com.yuyuko.idempotent.utils.ReflectionUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class IdempotentInterceptor implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentInterceptor.class);

    private IdempotentTemplate idempotentTemplate;

    public IdempotentInterceptor(IdempotentManager idempotentManager) {
        this.idempotentTemplate = new IdempotentTemplate(idempotentManager);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Class<?> targetClass = (methodInvocation.getThis() != null ?
                AopUtils.getTargetClass(methodInvocation.getThis()) : null);
        Method specificMethod = ClassUtils.getMostSpecificMethod(methodInvocation.getMethod(),
                targetClass);
        final Method method = BridgeMethodResolver.findBridgedMethod(specificMethod);

        final Idempotent idempotentAnnotation = ReflectionUtils.getAnnotation(method,
                Idempotent.class);
        if (idempotentAnnotation != null) {
            return handleIdempotent(method, methodInvocation, idempotentAnnotation);
        }
        return methodInvocation.proceed();
    }

    private Object handleIdempotent(Method method,
                                    MethodInvocation methodInvocation,
                                    Idempotent idempotentAnnotation) throws Throwable {
        Object[] args = methodInvocation.getArguments();
        try {
            return idempotentTemplate.execute(new AbstractIdempotentExecutor(method, args,
                    idempotentAnnotation) {
                @Override
                public Object execute() throws Throwable {
                    return methodInvocation.proceed();
                }
            });
        } catch (RejectedException ex) {
            logger.info("拒绝执行方法[{}],幂等操作id[{}]", method.getName(), ex.getId());
            return ExpressionResolver.resolveReturnVal(idempotentAnnotation.returnValWhenRejected());
        }
    }
}
