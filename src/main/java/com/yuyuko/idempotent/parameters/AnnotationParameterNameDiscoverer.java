package com.yuyuko.idempotent.parameters;

/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

    private final Set<String> annotationClassesToUse;

    public AnnotationParameterNameDiscoverer(String... annotationClassToUse) {
        this(new HashSet<>(Arrays.asList(annotationClassToUse)));
    }

    public AnnotationParameterNameDiscoverer(Set<String> annotationClassesToUse) {
        Assert.notEmpty(annotationClassesToUse,
                "annotationClassesToUse cannot be null or empty");
        this.annotationClassesToUse = annotationClassesToUse;
    }

    public String[] getParameterNames(Method method) {
        Method originalMethod = BridgeMethodResolver.findBridgedMethod(method);
        String[] paramNames = lookupParameterNames(METHOD_METHODPARAM_FACTORY,
                originalMethod);
        if (paramNames != null) {
            return paramNames;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?>[] interfaces = declaringClass.getInterfaces();
        for (Class<?> intrfc : interfaces) {
            Method intrfcMethod = ReflectionUtils.findMethod(intrfc, method.getName(),
                    method.getParameterTypes());
            if (intrfcMethod != null) {
                return lookupParameterNames(METHOD_METHODPARAM_FACTORY, intrfcMethod);
            }
        }
        return paramNames;
    }

    public String[] getParameterNames(Constructor<?> constructor) {
        return lookupParameterNames(CONSTRUCTOR_METHODPARAM_FACTORY, constructor);
    }

    private <T extends AccessibleObject> String[] lookupParameterNames(
            ParameterNameFactory<T> parameterNameFactory, T t) {
        Annotation[][] parameterAnnotations = parameterNameFactory.findParameterAnnotations(t);
        int parameterCount = parameterAnnotations.length;
        String[] paramNames = new String[parameterCount];
        boolean found = false;
        for (int i = 0; i < parameterCount; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            String parameterName = findParameterName(annotations);
            if (parameterName != null) {
                found = true;
                paramNames[i] = parameterName;
            }
        }
        return found ? paramNames : null;
    }

    private String findParameterName(Annotation[] parameterAnnotations) {
        for (Annotation paramAnnotation : parameterAnnotations) {
            if (annotationClassesToUse.contains(paramAnnotation.annotationType()
                    .getName())) {
                return (String) AnnotationUtils.getValue(paramAnnotation, "value");
            }
        }
        return null;
    }

    private static final ParameterNameFactory<Constructor<?>> CONSTRUCTOR_METHODPARAM_FACTORY = new ParameterNameFactory<Constructor<?>>() {

        public Annotation[][] findParameterAnnotations(Constructor<?> constructor) {
            return constructor.getParameterAnnotations();
        }
    };

    private static final ParameterNameFactory<Method> METHOD_METHODPARAM_FACTORY = new ParameterNameFactory<Method>() {

        public Annotation[][] findParameterAnnotations(Method method) {
            return method.getParameterAnnotations();
        }
    };

    private interface ParameterNameFactory<T extends AccessibleObject> {

        Annotation[][] findParameterAnnotations(T t);
    }
}

