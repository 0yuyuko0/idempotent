package com.yuyuko.idempotent.api;

import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.expression.ExpressionResolver;
import com.yuyuko.idempotent.parameters.MethodIdempotentEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractIdempotentExecutor implements IdempotentExecutor {
    private IdempotentInfo idempotentInfo;

    public AbstractIdempotentExecutor(Method method, Object[] args,
                                      Idempotent idempotentAnnotation) {
        this.idempotentInfo =
                IdempotentInfo.IdempotentInfoBuilder.build(idempotentAnnotation,
                        ExpressionResolver.resolveId(method, args, idempotentAnnotation.id()));
    }

    @Override
    public abstract Object execute() throws Throwable;

    @Override
    public IdempotentInfo getIdempotentInfo() {
        return idempotentInfo;
    }
}
