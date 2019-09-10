package com.yuyuko.idempotent;

import com.yuyuko.idempotent.bean.TestBean;
import com.yuyuko.idempotent.redis.RedisUtils;
import com.yuyuko.idempotent.bean.TestPOJO;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IdempotentApplicationTests {
    @Autowired
    TestBean testBean;

    @Autowired
    RedisUtils redisUtils;

    @Test
    public void contextLoads() {
        testBean.hello(new TestPOJO("yuyuko"));
        testBean.hello(new TestPOJO("yuyuko"));
    }

    @After
    public void after() {
        redisUtils.delete("idem:yuyuko");
    }

}
