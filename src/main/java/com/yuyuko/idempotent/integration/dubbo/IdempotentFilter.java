package com.yuyuko.idempotent.integration.dubbo;

import com.yuyuko.idempotent.RejectedException;
import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.api.AbstractIdempotentExecutor;
import com.yuyuko.idempotent.api.IdempotentTemplate;
import com.yuyuko.idempotent.expression.ExpressionResolver;
import com.yuyuko.idempotent.utils.ReflectionUtils;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


@Activate(group = Constants.PROVIDER, order = 100)
public class IdempotentFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentFilter.class);

    private IdempotentTemplate idempotentTemplate;

    public void setIdempotentTemplate(IdempotentTemplate idempotentTemplate) {
        this.idempotentTemplate = idempotentTemplate;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Method method = ReflectionUtils.getSpecificMethod(
                invoker.getInterface(),
                invocation.getMethodName(),
                invocation.getParameterTypes());

        Idempotent idempotentAnnotation = ReflectionUtils.getAnnotation(method, Idempotent.class);
        if (idempotentAnnotation != null) {
            return handleIdempotent(method, invoker, invocation, idempotentAnnotation);
        }
        return invoker.invoke(invocation);
    }

    private Result handleIdempotent(Method method,
                                    Invoker<?> invoker,
                                    Invocation invocation,
                                    Idempotent idempotentAnnotation) throws RpcException {
        Object[] args = invocation.getArguments();
        try {
            return (Result) idempotentTemplate.execute(new AbstractIdempotentExecutor(method, args,
                    idempotentAnnotation) {
                @Override
                public Result execute() throws RpcException {
                    return invoker.invoke(invocation);
                }
            });
        } catch (RejectedException ex) {
            logger.info("拒绝执行方法[{}],幂等操作id[{}]", method.getName(), ex.getId());
            return new RpcResult(ExpressionResolver.resolveReturnVal(idempotentAnnotation.returnValWhenRejected()));
        } catch (Throwable throwable) {
            if (throwable instanceof RpcException)
                throw (RpcException) throwable;
            if (throwable instanceof RuntimeException)
                throw (RuntimeException) throwable;
        }
        return null;
    }
}
