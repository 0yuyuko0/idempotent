package com.yuyuko.idempotent.parameters;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class MethodIdempotentEvaluationContext extends StandardEvaluationContext {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodIdempotentEvaluationContext.class);

    private ParameterNameDiscoverer parameterNameDiscoverer;
    private final MethodInvocation mi;

    public MethodIdempotentEvaluationContext(MethodInvocation mi) {
        this(mi, new IdempotentParameterNameDiscoverer());
    }

    public MethodIdempotentEvaluationContext(MethodInvocation mi,
                                             ParameterNameDiscoverer parameterNameDiscoverer) {
        this.mi = mi;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public Object lookupVariable(String name) {
        Object variable = super.lookupVariable(name);

        if (variable != null)
            return variable;

        addArgumentsAsVariables();

        variable = super.lookupVariable(name);

        return variable;
    }

    private void addArgumentsAsVariables() {
        Object[] args = mi.getArguments();

        if (args.length == 0) {
            return;
        }

        Object targetObject = mi.getThis();
        // SEC-1454
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(targetObject);

        if (targetClass == null) {
            // TODO: Spring should do this, but there's a bug in ultimateTargetClass()
            // which returns null
            targetClass = targetObject.getClass();
        }

        Method method = AopUtils.getMostSpecificMethod(mi.getMethod(), targetClass);
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);

        if (paramNames == null) {
            logger.warn("Unable to resolve method parameter names for method: "
                    + method
                    + ". Debug symbol information is required if you are using parameter names in" +
                    " expressions.");
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (paramNames[i] != null) {
                setVariable(paramNames[i], args[i]);
            }
        }
    }
}
