package com.yuyuko.idempotent.spring.autoconfigure;

import com.yuyuko.idempotent.api.IdempotentApi;
import com.yuyuko.idempotent.api.IdempotentManager;
import com.yuyuko.idempotent.api.IdempotentTemplate;
import com.yuyuko.idempotent.redis.RedisUtils;
import com.yuyuko.idempotent.spring.IdempotentScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class IdempotentAutoConfiguration {
    @Bean
    public IdempotentScanner idempotentScanner() {
        return new IdempotentScanner(idempotentManager(idempotentRedisUtils()));
    }

    @Bean
    public IdempotentApi idempotentApi(IdempotentManager idempotentManager) {
        return new IdempotentApi(idempotentManager);
    }

    @Autowired
    RedisConnectionFactory connectionFactory;

    @Bean
    public IdempotentManager idempotentManager(RedisUtils redisUtils) {
        return new IdempotentManager(redisUtils);
    }

    @Bean
    public RedisUtils idempotentRedisUtils() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return new RedisUtils(redisTemplate);
    }
}