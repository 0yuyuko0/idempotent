package com.yuyuko.idempotent.dubbo.api.impl;

import com.yuyuko.idempotent.dubbo.api.TestApiProvider;
import com.yuyuko.idempotent.spring.TestPOJO;
import org.apache.dubbo.config.annotation.Service;

@Service
public class TestApiProviderImpl implements TestApiProvider {
    @Override
    public void test(TestPOJO testPOJO) {
        System.out.println(String.format("hello %s\n", testPOJO.getUsername()));
    }
}
