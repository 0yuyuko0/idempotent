package com.yuyuko.idempotent.dubbo;

import com.yuyuko.idempotent.dubbo.api.TestApiProvider;
import com.yuyuko.idempotent.spring.TestPOJO;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Component;

@Component
public class TestApiConsumer {
    @Reference
    TestApiProvider apiProvider;

    public void hello(TestPOJO testPOJO) {
        apiProvider.test(testPOJO);
    }
}
