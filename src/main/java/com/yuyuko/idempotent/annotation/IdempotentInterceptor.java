package com.yuyuko.idempotent.annotation;

import com.yuyuko.idempotent.api.*;
import com.yuyuko.idempotent.DenyException;
import com.yuyuko.idempotent.parameters.MethodIdempotentEvaluationContext;
import com.yuyuko.idempotent.redis.RedisUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private SpelExpressionParser parser;

    private IdempotentTemplate idempotentTemplate;

    public IdempotentInterceptor(RedisUtils idempotentRedisUtils) {
        parser = new SpelExpressionParser();
        idempotentTemplate = new IdempotentTemplate(idempotentRedisUtils);
    }

    private String getIdempotentIdentifier(MethodInvocation mi, String idString) {
        if (idString == null || idString.equals(""))
            throw new IllegalArgumentException("id不能为空");
        EvaluationContext context = new MethodIdempotentEvaluationContext(mi);
        Expression expression = parser.parseExpression(idString);
        return expression.getValue(context, String.class);
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
                return handleIdempotent(methodInvocation, idempotentAnnotation);
            } catch (DenyException ex) {
                logger.info("拒绝执行方法[{}],幂等操作id[{}]", method.getName(), ex.getId());
                return null;
            }
        }
        return methodInvocation.proceed();
    }

    private Object handleIdempotent(MethodInvocation methodInvocation,
                                    Idempotent idempotentAnnotation) throws Throwable {
        return idempotentTemplate.execute(new IdempotentExecutor() {
            @Override
            public Object execute() throws Throwable {
                return methodInvocation.proceed();
            }

            @Override
            public IdempotentInfo getIdempotentInfo() {
                IdempotentInfo idempotentInfo = new IdempotentInfo();
                idempotentInfo.setId(
                        getIdempotentIdentifier(methodInvocation, idempotentAnnotation.id())
                );
                idempotentInfo.setMaxExecutionTime(idempotentAnnotation.maxExecutionTime());
                idempotentInfo.setDuration(idempotentAnnotation.duration());
                idempotentInfo.setPrefix(idempotentAnnotation.prefix());
                Set<RollbackRule> rollbackRules = new LinkedHashSet<>();
                for (Class<?> rbRule : idempotentAnnotation.rollbackFor()) {
                    rollbackRules.add(new RollbackRule(rbRule));
                }
                for (String rbRule : idempotentAnnotation.rollbackForClassName()) {
                    rollbackRules.add(new RollbackRule(rbRule));
                }
                for (Class<?> rbRule : idempotentAnnotation.noRollbackFor()) {
                    rollbackRules.add(new NoRollbackRule(rbRule));
                }
                for (String rbRule : idempotentAnnotation.noRollbackForClassName()) {
                    rollbackRules.add(new NoRollbackRule(rbRule));
                }
                idempotentInfo.setRollbackRules(rollbackRules);
                return idempotentInfo;
            }
        });
    }

    private <T extends Annotation> T getAnnotation(Method method, Class<T> clazz) {
        return method == null ? null : method.getAnnotation(clazz);
    }
}
