package com.yuyuko.idempotent.spring;

import com.yuyuko.idempotent.spring.utils.SpringProxyUtils;
import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.api.IdempotentTemplate;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class IdempotentScanner extends AbstractAutoProxyCreator {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentScanner.class);

    private MethodInterceptor interceptor;

    /**
     * 已经代理过的bean的set
     */
    private static final Set<String> PROXY_SET = new HashSet<>();

    public IdempotentScanner(IdempotentTemplate idempotentTemplate) {
        interceptor = new IdempotentInterceptor(idempotentTemplate);
    }

    @Override
    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        try {
            synchronized (PROXY_SET) {
                if(PROXY_SET.contains(beanName))
                    return bean;
                Class<?> targetClass = SpringProxyUtils.findTargetClass(bean);
                if (!existsAnnotation(targetClass))
                    return bean;
                logger.info(
                        "Bean[" + bean.getClass().getName() + "] with name [" + beanName + "] would " +
                                "use interceptor ["
                                + interceptor.getClass().getName() + "]");
                if (!AopUtils.isAopProxy(bean))
                    bean = super.wrapIfNecessary(bean, beanName, cacheKey);
                else{
                    AdvisedSupport advised = SpringProxyUtils.getAdvisedSupport(bean);
                    Advisor[] advisor = buildAdvisors(beanName, getAdvicesAndAdvisorsForBean(null, null, null));
                    for (Advisor avr : advisor) {
                        advised.addAdvisor(0, avr);
                    }
                }
                PROXY_SET.add(beanName);
                return bean;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean existsAnnotation(Class<?> clazz) {
        if (clazz == null)
            return false;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            Idempotent idemAnno = method.getAnnotation(Idempotent.class);
            if (idemAnno != null)
                return true;
        }
        return false;
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> aClass, String s,
                                                    TargetSource targetSource) throws BeansException {
        return new Object[]{interceptor};
    }
}
