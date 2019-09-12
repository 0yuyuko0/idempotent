package com.yuyuko.idempotent.integration.dubbo;

import com.yuyuko.idempotent.DenyException;
import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.api.AbstractIdempotentExecutor;
import com.yuyuko.idempotent.api.IdempotentTemplate;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;

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
        Method method = getSpecificMethod(
                invoker.getInterface(),
                invocation.getMethodName(),
                invocation.getParameterTypes());

        Idempotent idempotentAnnotation = getAnnotation(method, Idempotent.class);
        if (idempotentAnnotation != null) {
            try {
                return (Result) handleIdempotent(method, invoker, invocation, idempotentAnnotation);
            } catch (DenyException ex) {
                logger.info("拒绝执行方法[{}],幂等操作id[{}]", method.getName(), ex.getId());
                //带副作用的操作不应该有返回值，否则无法幂等
                return new RpcResult();
            } catch (Throwable throwable) {
                if (throwable instanceof RpcException)
                    throw (RpcException) throwable;
                if (throwable instanceof RuntimeException)
                    throw (RuntimeException) throwable;
            }
        }

        return invoker.invoke(invocation);
    }

    private Object handleIdempotent(Method method,
                                    Invoker<?> invoker,
                                    Invocation invocation,
                                    Idempotent idempotentAnnotation) throws Throwable {
        Object[] args = invocation.getArguments();
        return idempotentTemplate.execute(new AbstractIdempotentExecutor(method, args,
                idempotentAnnotation) {
            @Override
            public Object execute() throws RpcException {
                return invoker.invoke(invocation);
            }
        });
    }

    private static Method getSpecificMethod(Class<?> clazz,
                                            String methodName,
                                            Class<?>[] parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Annotation> T getAnnotation(Method method, Class<T> clazz) {
        return method == null ? null : method.getAnnotation(clazz);
    }
}
