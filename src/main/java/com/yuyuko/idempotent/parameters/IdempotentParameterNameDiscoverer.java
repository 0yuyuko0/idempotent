package com.yuyuko.idempotent.parameters;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;

import java.util.HashSet;
import java.util.Set;

public class IdempotentParameterNameDiscoverer extends PrioritizedParameterNameDiscoverer {
    public IdempotentParameterNameDiscoverer() {
        Set<String> annotationClassesToUse = new HashSet<>(2);
        annotationClassesToUse.add("com.yuyuko.mall.idempotent.parameters.Id");
        annotationClassesToUse.add(Id.class.getName());

        addDiscoverer(new AnnotationParameterNameDiscoverer(annotationClassesToUse));
        addDiscoverer(new DefaultParameterNameDiscoverer());
    }
}
