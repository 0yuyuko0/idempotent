package com.yuyuko.idempotent.spring.utils;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.EmptyTargetSource;

import java.lang.reflect.Field;

public class SpringProxyUtils {
    public static Class<?> findTargetClass(Object proxy) throws Exception {
        if (AopUtils.isAopProxy(proxy)) {
            AdvisedSupport advised = getAdvisedSupport(proxy);
            if (AopUtils.isJdkDynamicProxy(proxy)) {
                TargetSource targetSource = advised.getTargetSource();
                return targetSource instanceof EmptyTargetSource ? getFirstInterfaceByAdvised(advised)
                        : targetSource.getTargetClass();
            }
            Object target = advised.getTargetSource().getTarget();
            return findTargetClass(target);
        } else {
            return proxy == null ? null :proxy.getClass();
        }
    }

    public static AdvisedSupport getAdvisedSupport(Object proxy) throws Exception {
        Field h;
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            h = proxy.getClass().getSuperclass().getDeclaredField("h");
        } else {
            h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        }
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        return (AdvisedSupport)advised.get(dynamicAdvisedInterceptor);
    }

    private static Class<?> getFirstInterfaceByAdvised(AdvisedSupport advised) {
        Class<?>[] interfaces = advised.getProxiedInterfaces();
        if (interfaces.length > 0) {
            return interfaces[0];
        } else {
            throw new IllegalStateException("Find the jdk dynamic proxy class that does not implement the interface");
        }
    }
}
