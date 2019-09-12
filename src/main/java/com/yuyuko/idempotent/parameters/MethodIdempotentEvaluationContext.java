package com.yuyuko.idempotent.parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class MethodIdempotentEvaluationContext extends StandardEvaluationContext {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodIdempotentEvaluationContext.class);

    private ParameterNameDiscoverer parameterNameDiscoverer;
    private final Method method;

    private final Object[] args;

    public MethodIdempotentEvaluationContext(Method method, Object[] args) {
        this(method, args, new IdempotentParameterNameDiscoverer());
    }

    public MethodIdempotentEvaluationContext(Method method,
                                             Object[] args,
                                             ParameterNameDiscoverer parameterNameDiscoverer) {
        this.method = method;
        this.args = args;
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
        if (args.length == 0) {
            return;
        }

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
