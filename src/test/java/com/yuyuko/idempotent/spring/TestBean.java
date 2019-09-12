package com.yuyuko.idempotent.spring;

import com.yuyuko.idempotent.annotation.Idempotent;
import org.springframework.stereotype.Component;

@Component
public class TestBean {
    @Idempotent(id = "#testPOJO.getUsername()", rollbackFor = RuntimeException.class)
    public void hello(TestPOJO testPOJO) {
        System.out.println(String.format("hello %s\n", testPOJO.getUsername()));
    }
}