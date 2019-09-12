package com.yuyuko.idempotent.api;

import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.parameters.MethodIdempotentEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractIdempotentExecutor implements IdempotentExecutor {
    private IdempotentInfo idempotentInfo;

    private SpelExpressionParser parser = new SpelExpressionParser();

    public AbstractIdempotentExecutor(Method method, Object[] args,
                                      Idempotent idempotentAnnotation) {
        IdempotentInfo idempotentInfo = new IdempotentInfo();
        idempotentInfo.setId(
                getIdempotentIdentifier(method, args, idempotentAnnotation.id())
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
        this.idempotentInfo = idempotentInfo;
    }

    @Override
    public abstract Object execute() throws Throwable;

    public String getIdempotentIdentifier(Method method, Object[] args, String idString) {
        if (idString == null || idString.equals(""))
            throw new IllegalArgumentException("id不能为空");
        EvaluationContext context =
                new MethodIdempotentEvaluationContext(
                        method,
                        args);
        Expression expression = parser.parseExpression(idString);
        return expression.getValue(context, String.class);
    }


    @Override
    public IdempotentInfo getIdempotentInfo() {
        return idempotentInfo;
    }
}
