package com.yuyuko.idempotent.dubbo.api;

import com.yuyuko.idempotent.annotation.Idempotent;
import com.yuyuko.idempotent.spring.TestPOJO;

public interface TestApiProvider {
    @Idempotent(id = "#testPOJO.getUsername()", rollbackFor = RuntimeException.class)
    void test(TestPOJO testPOJO);
}
