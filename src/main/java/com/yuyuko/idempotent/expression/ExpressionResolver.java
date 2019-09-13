package com.yuyuko.idempotent.expression;

import com.yuyuko.idempotent.parameters.MethodIdempotentEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class ExpressionResolver {
    private static final SpelExpressionParser parser = new SpelExpressionParser();

    public static String resolveId(Method method, Object[] args, String idExpression) {
        if (idExpression == null || idExpression.equals(""))
            throw new IllegalArgumentException("id不能为空");
        EvaluationContext context =
                new MethodIdempotentEvaluationContext(
                        method,
                        args);
        Expression expression = parser.parseExpression(idExpression);
        return expression.getValue(context, String.class);
    }

    public static Object resolveReturnVal(String expression) {
        return parser.parseExpression(expression).getValue(new StandardEvaluationContext());
    }
}
