package com.yuyuko.idempotent.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Method getSpecificMethod(Class<?> clazz,
                                            String methodName,
                                            Class<?>[] parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Annotation> T getAnnotation(Method method, Class<T> clazz) {
        return method == null ? null : method.getAnnotation(clazz);
    }
}
